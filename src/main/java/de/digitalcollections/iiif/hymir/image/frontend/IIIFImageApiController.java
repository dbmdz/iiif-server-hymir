package de.digitalcollections.iiif.hymir.image.frontend;

import com.google.common.base.Strings;
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
    String base = String.format("%s://%s", scheme, host);
    if (!request.getContextPath().isEmpty()) {
      base += request.getContextPath();
    }
    return base;
  }

  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = "{identifier}/{region}/{size}/{rotation}/{quality}.{format}")
  public ResponseEntity<byte[]> getImageRepresentation(
          @PathVariable String identifier, @PathVariable String region,
          @PathVariable String size, @PathVariable String rotation,
          @PathVariable String quality, @PathVariable String format,
          HttpServletRequest request, HttpServletResponse response) throws ResolvingException,
          UnsupportedFormatException, UnsupportedOperationException, IOException,
          URISyntaxException, InvalidParametersException, ResourceNotFoundException {
    HttpHeaders headers = new HttpHeaders();
    String path;
    if (request.getPathInfo() != null) {
      path = request.getPathInfo();
    } else {
      path = request.getServletPath();
    }

    ImageApiSelector selector = new ImageApiSelector();
    selector.setIdentifier(identifier);
    selector.setRegion(region);
    selector.setSize(size);
    selector.setRotation(rotation);
    if (quality.equals("native")) {
      quality = "default";
    }
    selector.setQuality(ImageApiProfile.Quality.valueOf(quality.toUpperCase()));
    selector.setFormat(ImageApiProfile.Format.valueOf(format.toUpperCase()));

    de.digitalcollections.iiif.model.image.ImageService info = new de.digitalcollections.iiif.model.image.ImageService(
        "http://foo.org/" + identifier);
    imageService.readImageInfo(identifier, info);
    String canonicalForm = selector.getCanonicalForm(
        new Dimension(info.getWidth(), info.getHeight()),
        ImageApiProfile.LEVEL_TWO,
        ImageApiProfile.Quality.COLOR); // TODO: Make this variable on the actual image
    if (!canonicalForm.equals(selector.toString())) {
      String canonicalUrl = getUrlBase(request) + path.substring(0, path.indexOf(identifier)) + canonicalForm;
      response.sendRedirect(canonicalUrl);
      return null;
    } else {
      final String mimeType = selector.getFormat().getMimeType().getTypeName();
      headers.setContentType(MediaType.parseMediaType(mimeType));

      String filename = path.replaceFirst("/image/", "").replace('/', '_').replace(',', '_');
      headers.set("Content-Disposition", "inline; filename=" + filename);

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      imageService.processImage(identifier, selector, os);
      return new ResponseEntity<byte[]>(os.toByteArray(), headers, HttpStatus.OK);
    }
  }

  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = "{identifier}/info.json",
          method = {RequestMethod.GET, RequestMethod.HEAD})
  public ResponseEntity<String> getInfo(@PathVariable String identifier,
          HttpServletRequest request) throws Exception {
    identifier = URLDecoder.decode(identifier, "UTF-8");
    String path;
    if (request.getPathInfo() != null) {
      path = request.getPathInfo();
    } else {
      path = request.getServletPath();
    }
    String baseUrl = getUrlBase(request);
    de.digitalcollections.iiif.model.image.ImageService info = new de.digitalcollections.iiif.model.image.ImageService(
        baseUrl + path.replace("/info.json", ""));
    imageService.readImageInfo(identifier, info);

    HttpHeaders headers = new HttpHeaders();
    String contentType = request.getHeader("Accept");
    if (contentType != null && contentType.equals("application/ld+json")) {
      headers.set("Content-Type", contentType);
    } else {
      headers.set("Content-Type", "application/json");
      headers.add("Link", "<http://iiif.io/api/image/2/context.json>; "
              + "rel=\"http://www.w3.org/ns/json-ld#context\"; "
              + "type=\"application/ld+json\"");
    }
    headers.add("Link", String.format("<%s>;rel=\"profile\"", info.getProfiles().get(0).getIdentifier().toString()));
    return new ResponseEntity<>(objectMapper.writeValueAsString(info), headers, HttpStatus.OK);
  }

  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = "{identifier}", method = {RequestMethod.GET, RequestMethod.HEAD})
  public String getInfoRedirect(@PathVariable String identifier) {
    return "redirect:/image/" + VERSION + "/" + identifier + "/info.json";
  }
}
