package de.digitalcollections.iiif.hymir.image.frontend;

import de.digitalcollections.iiif.hymir.image.business.api.ImageService;
import de.digitalcollections.iiif.hymir.model.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.model.exception.ResourceNotFoundException;
import de.digitalcollections.iiif.hymir.model.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.model.image.ImageApiProfile;
import de.digitalcollections.iiif.model.image.ImageApiSelector;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping("/image/v2/")
public class IIIFImageApiController {

  private static final Logger LOGGER = LoggerFactory.getLogger(IIIFImageApiController.class);
  public static final String VERSION = "v2";

  @Autowired
  private ImageService imageService;

  @Autowired
  private IiifObjectMapper objectMapper;

  private String getUrlBase(HttpServletRequest request) {
    String requestURI = request.getRequestURI();

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
          HttpServletRequest request, HttpServletResponse response, WebRequest webRequest) throws ResolvingException,
          UnsupportedFormatException, UnsupportedOperationException, IOException,
          URISyntaxException, InvalidParametersException, ResourceNotFoundException {
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
    } catch (IllegalArgumentException e) {
      throw new InvalidParametersException(e.getMessage());
    }
    de.digitalcollections.iiif.model.image.ImageService info = new de.digitalcollections.iiif.model.image.ImageService(
        "http://foo.org/" + identifier);
    imageService.readImageInfo(identifier, info);
    String canonicalForm;
    try {
      canonicalForm = selector.getCanonicalForm(
          new Dimension(info.getWidth(), info.getHeight()),
          ImageApiProfile.LEVEL_TWO,
          ImageApiProfile.Quality.COLOR); // TODO: Make this variable on the actual image
    } catch (IllegalArgumentException e) {
      throw new InvalidParametersException(e.getMessage());
    }
    String canonicalUrl = getUrlBase(request) + path.substring(0, path.indexOf(identifier)) + canonicalForm;
    if (!canonicalForm.equals(selector.toString())) {
      response.setHeader("Link", String.format("<%s>;rel=\"canonical\"", canonicalUrl));
      response.sendRedirect(canonicalUrl);
      return null;
    } else {
      headers.add("Link", String.format("<%s>;rel=\"canonical\"", canonicalUrl));
      final String mimeType = selector.getFormat().getMimeType().getTypeName();
      headers.setContentType(MediaType.parseMediaType(mimeType));

      String filename = path.replaceFirst("/image/", "").replace('/', '_').replace(',', '_');
      headers.set("Content-Disposition", "inline; filename=" + filename);
      headers.add("Link", String.format("<%s>;rel=\"profile\"", info.getProfiles().get(0).getIdentifier().toString()));

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      imageService.processImage(identifier, selector, os);
      return new ResponseEntity<byte[]>(os.toByteArray(), headers, HttpStatus.OK);
    }
  }

  @RequestMapping(value = "{identifier}/info.json",
          method = {RequestMethod.GET, RequestMethod.HEAD})
  public ResponseEntity<String> getInfo(@PathVariable String identifier, HttpServletRequest req,
          WebRequest webRequest) throws Exception {
    identifier = URLDecoder.decode(identifier, "UTF-8");
    long modified = imageService.getImageModificationDate(identifier).toEpochMilli();
    webRequest.checkNotModified(modified);
    String path;
    if (req.getPathInfo() != null) {
      path = req.getPathInfo();
    } else {
      path = req.getServletPath();
    }
    String baseUrl = getUrlBase(req);
    de.digitalcollections.iiif.model.image.ImageService info = new de.digitalcollections.iiif.model.image.ImageService(
        baseUrl + path.replace("/info.json", ""));
    imageService.readImageInfo(identifier, info);

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
    // We set the header ourselves, since using @CrossOrigin doesn't expose "*", but always sets the requesting domain
    headers.add("Access-Control-Allow-Origin", "*");
    return new ResponseEntity<>(objectMapper.writeValueAsString(info), headers, HttpStatus.OK);
  }

  @RequestMapping(value = "{identifier}", method = {RequestMethod.GET, RequestMethod.HEAD})
  public String getInfoRedirect(@PathVariable String identifier, HttpServletResponse response) {
    response.setHeader("Access-Control-Allow-Origin", "*");
    return "redirect:/image/" + VERSION + "/" + identifier + "/info.json";
  }
}
