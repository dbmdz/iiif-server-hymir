package de.digitalcollections.iiif.hymir.image.frontend.impl.springmvc.controller.v2;

import de.digitalcollections.iiif.hymir.image.business.api.service.v2.ImageService;
import de.digitalcollections.iiif.hymir.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.api.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.model.api.exception.ResourceNotFoundException;
import de.digitalcollections.iiif.hymir.model.api.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.model.image.ImageApiProfile;
import de.digitalcollections.iiif.model.image.ImageApiSelector;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
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

@Controller(value = "IIIFImageApiController-v2")
@RequestMapping("/image/v2/")
public class IIIFImageApiController {

  private static final Logger LOGGER = LoggerFactory.getLogger(IIIFImageApiController.class);
  public static final String VERSION = "v2";

  @Autowired
  private ImageService imageService;

  @Autowired
  private IiifObjectMapper objectMapper;

  private String getBasePath(HttpServletRequest request, String identifier) {
    String requestURI = request.getRequestURI();
    if (requestURI.isEmpty()) {
      requestURI = "/" + identifier + "/"; // For unit-tests
    }
    String idEndpoint = requestURI.substring(0, requestURI.lastIndexOf('/'));

    String scheme = request.getHeader("X-Forwarded-Proto");
    if (scheme == null) {
      scheme = request.getScheme();
    }

    String host = request.getHeader("X-Forwarded-Host");
    if (host == null) {
      host = request.getHeader("Host");
    }

    return String.format("%s://%s%s", scheme, host, idEndpoint);
  }

  /**
   * Specification see <a href="http://iiif.io/api/image/2.0/#image-request-uri-syntax">IIIF 2.0</a><br>
   *
   * @throws ResolvingException if identifier can not be resolved to an image
   * @throws UnsupportedFormatException if target format is not supported
   * @throws UnsupportedOperationException if operation is not supported
   * @throws IOException if image can not be read
   * @throws URISyntaxException if uri for image is erroneous
   * @throws InvalidParametersException if parameters can not be parsed
   */
  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = "{identifier}/{region}/{size}/{rotation}/{quality}.{format}")
  public ResponseEntity<byte[]> getImageRepresentation(
          @PathVariable String identifier, @PathVariable String region,
          @PathVariable String size, @PathVariable String rotation,
          @PathVariable String quality, @PathVariable String format,
          HttpServletRequest request) throws ResolvingException,
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
    selector.setRegion(region);
    selector.setSize(size);
    selector.setRotation(rotation);
    if (quality.equals("native")) {
      quality = "default";
    }
    selector.setQuality(ImageApiProfile.Quality.valueOf(quality.toUpperCase()));
    selector.setFormat(ImageApiProfile.Format.valueOf(format.toUpperCase()));

    final String mimeType = selector.getFormat().getMimeType().getTypeName();
    headers.setContentType(MediaType.parseMediaType(mimeType));

    String filename = path.replaceFirst("/image/", "").replace('/', '_').replace(',', '_');
    headers.set("Content-Disposition", "inline; filename=" + filename);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    imageService.processImage(identifier, selector, os);
    return new ResponseEntity<byte[]>(os.toByteArray(), headers, HttpStatus.OK);
  }

  @RequestMapping(value = "{identifier}")
  public void redirectToInfo(@PathVariable String ident, HttpServletResponse response) throws IOException {
    response.sendRedirect("/" + ident + "/info.json");
  }

  /**
   * Specification see: http://iiif.io/api/image/2.0/#image-information
   * <p>
   * Example response:</p>
   *
   * <pre>
   * {
   *   "@context": "http://library.stanford.edu/iiif/image-api/1.1/context.json",
   *   "@id": "http://iiif.example.com/prefix/1E34750D-38DB-4825-A38A-B60A345E591C",
   *   "width": 6000, "height": 4000
   * }
   * </pre>
   *
   * @param identifier - The identifier to obtain information for
   * @param request servlet request
   * @return The information in JSON notation
   * @throws ResolvingException if identifier can not be resolved to an image
   * @throws UnsupportedFormatException if target format is not supported
   */
  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = "{identifier}/info.json",
          method = {RequestMethod.GET, RequestMethod.HEAD})
  public ResponseEntity<String> getInfo(@PathVariable String identifier,
          HttpServletRequest request) throws Exception {
    identifier = URLDecoder.decode(identifier, "UTF-8");
    String baseUrl = getBasePath(request, identifier);
    de.digitalcollections.iiif.model.image.ImageService info = new de.digitalcollections.iiif.model.image.ImageService(baseUrl);
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
