package de.digitalcollections.iiif.hymir.image.frontend;

import de.digitalcollections.commons.springboot.metrics.MetricsService;
import de.digitalcollections.iiif.hymir.config.CustomResponseHeaders;
import de.digitalcollections.iiif.hymir.image.business.api.ImageService;
import de.digitalcollections.iiif.hymir.model.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.hymir.util.UrlRules;
import de.digitalcollections.iiif.model.image.ImageApiProfile;
import de.digitalcollections.iiif.model.image.ImageApiSelector;
import de.digitalcollections.iiif.model.image.ResolvingException;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceNotFoundException;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping("${custom.iiif.image.urlPrefix:/image/v2/}")
public class IIIFImageApiController {

  public static final String VERSION = "v2";

  @Value("${custom.iiif.image.canonicalRedirect:true}")
  private boolean isCanonicalRedirectEnabled;

  protected final CustomResponseHeaders customResponseHeaders;

  private final ImageService imageService;
  private final IiifObjectMapper objectMapper;

  private final MetricsService metricsService;

  @Autowired
  public IIIFImageApiController(ImageService imageService, IiifObjectMapper objectMapper, CustomResponseHeaders customResponseHeaders, MetricsService metricsService) {
    this.imageService = imageService;
    this.objectMapper = objectMapper;
    this.customResponseHeaders = customResponseHeaders;
    this.metricsService = metricsService;
  }

  void setCanonicalRedirectEnabled(boolean canonicalRedirectEnabled) {
    isCanonicalRedirectEnabled = canonicalRedirectEnabled;
  }

  /**
   * Get the base URL for all Image API URLs from the request.
   *
   * This will handle cases such as reverse-proxying and SSL-termination on the frontend server
   */
  private String getUrlBase(HttpServletRequest request) {
    String scheme = request.getHeader("X-Forwarded-Proto");
    if (scheme == null) {
      scheme = request.getScheme();
    }

    String host = request.getHeader("X-Forwarded-Host");
    if (host == null) {
      host = request.getHeader("Host");
    }
    if (host == null) {
      host = request.getRemoteHost();
    }
    String base = String.format("%s://%s", scheme, host);
    if (!request.getContextPath().isEmpty()) {
      base += request.getContextPath();
    }
    return base;
  }

  @RequestMapping(value = "{identifier}/{region}/{size}/{rotation}/{quality}.{format}")
  public ResponseEntity<byte[]> getImageRepresentation(
      @PathVariable String identifier, @PathVariable String region,
      @PathVariable String size, @PathVariable String rotation,
      @PathVariable String quality, @PathVariable String format,
      HttpServletRequest request, HttpServletResponse response, WebRequest webRequest)
      throws UnsupportedFormatException, UnsupportedOperationException, IOException, InvalidParametersException, ResourceNotFoundException {
    if (UrlRules.isInsecure(identifier)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new byte[]{});
    }
    HttpHeaders headers = new HttpHeaders();
    String path;
    if (request.getPathInfo() != null) {
      path = request.getPathInfo();
    } else {
      path = request.getServletPath();
    }

    long modified = imageService.getImageModificationDate(identifier).toEpochMilli();
    webRequest.checkNotModified(modified);
    headers.setDate("Last-Modified", modified);

    ImageApiSelector selector = new ImageApiSelector();
    try {
      selector.setIdentifier(identifier);
      selector.setRegion(region);
      selector.setSize(size);
      selector.setRotation(rotation);
      if (quality.equals("native")) {
        quality = "default";
      }
      selector.setQuality(ImageApiProfile.Quality.valueOf(quality.toUpperCase()));
      selector.setFormat(ImageApiProfile.Format.valueOf(format.toUpperCase()));
    } catch (ResolvingException e) {
      throw new InvalidParametersException(e);
    }
    var info = new de.digitalcollections.iiif.model.image.ImageService("http://foo.org/" + identifier);
    imageService.readImageInfo(identifier, info);
    ImageApiProfile profile = ImageApiProfile.merge(info.getProfiles());
    String canonicalForm;
    try {
      canonicalForm = selector.getCanonicalForm(
          new Dimension(info.getWidth(), info.getHeight()),
          profile, ImageApiProfile.Quality.COLOR); // TODO: Make this variable on the actual image
    } catch (ResolvingException e) {
      throw new InvalidParametersException(e);
    }
    String canonicalUrl = getUrlBase(request) + path.substring(0, path.indexOf(identifier)) + canonicalForm;
    if (this.isCanonicalRedirectEnabled && !canonicalForm.equals(selector.toString())) {
      response.setHeader("Link", String.format("<%s>;rel=\"canonical\"", canonicalUrl));
      response.sendRedirect(canonicalUrl);
      return null;
    } else {
      headers.add("Link", String.format("<%s>;rel=\"canonical\"", canonicalUrl));

      String filename = path.replaceFirst("/image/", "").replace('/', '_').replace(',', '_');
      headers.set("Content-Disposition", "inline; filename=" + filename);
      headers.add("Link", String.format("<%s>;rel=\"profile\"", info.getProfiles().get(0).getIdentifier().toString()));

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      long duration = System.currentTimeMillis();
      imageService.processImage(identifier, selector, profile, os);
      duration = System.currentTimeMillis() - duration;
      metricsService.increaseCounterWithDurationAndPercentiles("image", "process", duration);

      customResponseHeaders.forImageTile().forEach(customResponseHeader -> {
        headers.set(customResponseHeader.getName(), customResponseHeader.getValue());
      });
      final String mimeType = selector.getFormat().getMimeType().getTypeName();
      headers.setContentType(MediaType.parseMediaType(mimeType));
      return new ResponseEntity<>(os.toByteArray(), headers, HttpStatus.OK);
    }
  }

  @RequestMapping(value = "{identifier}/info.json", method = {RequestMethod.GET, RequestMethod.HEAD})
  public ResponseEntity<String> getInfo(@PathVariable String identifier, HttpServletRequest req,
                                        WebRequest webRequest) throws Exception {
    if (UrlRules.isInsecure(identifier)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
    }
    long duration = System.currentTimeMillis();
    long modified = imageService.getImageModificationDate(identifier).toEpochMilli();
    webRequest.checkNotModified(modified);
    String path;
    if (req.getPathInfo() != null) {
      path = req.getPathInfo();
    } else {
      path = req.getServletPath();
    }
    String baseUrl = getUrlBase(req);
    String imageIdentifier = baseUrl + path.replace("/info.json", "")
                                           .replace(identifier, URLEncoder.encode(identifier, StandardCharsets.UTF_8));
    var info = new de.digitalcollections.iiif.model.image.ImageService(imageIdentifier);
    imageService.readImageInfo(identifier, info);
    duration = System.currentTimeMillis() - duration;
    metricsService.increaseCounterWithDurationAndPercentiles("generations", "infojson", duration);
    HttpHeaders headers = new HttpHeaders();
    headers.setDate("Last-Modified", modified);
    String contentType = req.getHeader("Accept");
    if (contentType != null && contentType.equals("application/ld+json")) {
      headers.set("Content-Type", contentType);
    } else {
      headers.set("Content-Type", "application/json");
      headers.add("Link", "<http://iiif.io/api/image/2/context.json>; "
                          + "rel=\"http://www.w3.org/ns/json-ld#context\"; "
                          + "type=\"application/ld+json\"");
    }
    headers.add("Link", String.format("<%s>;rel=\"profile\"", info.getProfiles().get(0).getIdentifier().toString()));
    headers.add("Access-Control-Allow-Origin", "*");

    customResponseHeaders.forImageInfo().forEach(customResponseHeader -> {
      headers.set(customResponseHeader.getName(), customResponseHeader.getValue());
    });
    return new ResponseEntity<>(objectMapper.writeValueAsString(info), headers, HttpStatus.OK);
  }

  @RequestMapping(value = "{identifier}", method = {RequestMethod.GET, RequestMethod.HEAD})
  public String getInfoRedirect(@PathVariable String identifier, HttpServletResponse response) {
    if (UrlRules.isInsecure(identifier)) {
      response.setStatus(400);
      return null;
    }
    response.setHeader("Access-Control-Allow-Origin", "*");
    return "redirect:/image/" + VERSION + "/" + identifier + "/info.json";
  }
}
