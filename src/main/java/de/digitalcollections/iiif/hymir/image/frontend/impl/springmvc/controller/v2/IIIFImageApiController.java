package de.digitalcollections.iiif.hymir.image.frontend.impl.springmvc.controller.v2;

import de.digitalcollections.commons.server.HttpLoggingUtilities;
import de.digitalcollections.iiif.hymir.image.backend.impl.repository.jpegtran.v2.JpegTranImage;
import de.digitalcollections.iiif.hymir.image.business.api.service.v2.IiifParameterParserService;
import de.digitalcollections.iiif.hymir.image.business.api.service.v2.ImageService;
import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.hymir.image.model.api.v2.Image;
import de.digitalcollections.iiif.hymir.image.model.api.v2.ImageInfo;
import de.digitalcollections.iiif.hymir.image.model.api.v2.RegionParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.ResizeParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.RotationParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.TransformationException;
import de.digitalcollections.iiif.hymir.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.api.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.model.api.exception.ResourceNotFoundException;
import de.digitalcollections.iiif.hymir.model.api.exception.UnsupportedFormatException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.stream.IntStream;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
  private IiifParameterParserService iiifParameterParserService;

  @Autowired
  private ImageService imageService;

  private final String IIIF_COMPLIANCE = "http://iiif.io/api/image/2/level2.json";
  private final String IIIF_CONTEXT = "http://iiif.io/api/image/2/context.json";

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
   * see <a href="http://iiif.io/api/image/2.0/#image-request-uri-syntax">IIIF 2.0</a><br>
   *
   * The sequence of parameters in the URI is intended as a mnemonic for the order in which image manipulations are made
   * against the full image content. This is important to consider when implementing the service because applying the
   * same parameters in a different sequence will often result in a different image being delivered. The order is
   * critical so that the application calling the service reliably receives the output it expects.<br>
   * The parameters should be interpreted as if the the sequence of image manipulations were:<br>
   * <b>Region THEN Size THEN Rotation THEN Quality THEN Format</b><br>
   * If the rotation parameter includes mirroring (“!”), the mirroring is applied before the rotation.
   *
   * @param identifier unique identifier of image
   * @param region The region parameter defines the rectangular portion of the full image to be returned. Region can be
   * specified by pixel coordinates, percentage or by the value “full”, which specifies that the entire image should be
   * returned.
   * @param size The size parameter determines the dimensions to which the extracted region is to be scaled.
   * @param rotation The rotation parameter specifies mirroring and rotation. A leading exclamation mark (“!”) indicates
   * that the image should be mirrored by reflection on the vertical axis before any rotation is applied. The numerical
   * value represents the number of degrees of clockwise rotation, and may be any floating point number from 0 to 360.
   * @param quality The quality parameter determines whether the image is delivered in color, grayscale or black and
   * white.
   * @param format The format of the returned image is expressed as an extension at the end of the URI.
   * @param request http request
   * @return the transformed image
   * @throws ResolvingException if identifier can not be resolved to an image
   * @throws UnsupportedFormatException if target format is not supported
   * @throws UnsupportedOperationException if operation is not supported
   * @throws IOException if image can not be read
   * @throws URISyntaxException if uri for image is erroneous
   * @throws InvalidParametersException if parameters can not be parsed
   * @throws TransformationException if image can not be transformed
   */
  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = "{identifier}/{region}/{size}/{rotation}/{quality}.{format}")
  public ResponseEntity<byte[]> getImageRepresentation(
          @PathVariable String identifier, @PathVariable String region,
          @PathVariable String size, @PathVariable String rotation,
          @PathVariable String quality, @PathVariable String format,
          HttpServletRequest request) throws ResolvingException,
          UnsupportedFormatException, UnsupportedOperationException, IOException,
          URISyntaxException, InvalidParametersException, TransformationException, ResourceNotFoundException {
    HttpHeaders headers = new HttpHeaders();

    HttpLoggingUtilities.addRequestClientInfoToMDC(request);
    MDC.put("iiifFormat", format);
    MDC.put("iiifQuality", quality);
    MDC.put("iiifRotation", rotation);
    MDC.put("iiifSize", size);
    MDC.put("iiifRegion", region);
    MDC.put("imageId", identifier);

    String path;
    if (request.getPathInfo() != null) {
      path = request.getPathInfo();
    } else {
      path = request.getServletPath();
    }

    try {
      RegionParameters regionParameters = iiifParameterParserService.parseIiifRegion(region);
      ResizeParameters sizeParameters = iiifParameterParserService.parseIiifSize(size);
      RotationParameters rotationParameters = iiifParameterParserService.parseIiifRotation(rotation);
      ImageBitDepth bitDepthParameter = iiifParameterParserService.parseIiifQuality(quality);
      ImageFormat formatParameter = iiifParameterParserService.parseIiifFormat(format);

      Image image = imageService.processImage(identifier, regionParameters, sizeParameters,
                                              rotationParameters, bitDepthParameter, formatParameter);

      // header
      final ImageFormat imageFormat = image.getFormat();
      final String mimeType = imageFormat.getMimeType();
      headers.setContentType(MediaType.parseMediaType(mimeType));

      String filename = path.replaceFirst("/image/", "").replace('/', '_').replace(',', '_');
      headers.set("Content-Disposition", "inline; filename=" + filename);
      // content
      byte[] data = image.toByteArray();

      MDC.put("imageBackend", (image instanceof JpegTranImage) ? "turbojpeg" : "imageio");
      LOGGER.info("Successfully served image for {}", path);
      headers.set("X-IIIF-Image-Backend", image instanceof JpegTranImage ? "fast" : "slow");
      return new ResponseEntity<>(data, headers, HttpStatus.OK);
    } catch (InvalidParametersException ex) {
      LOGGER.info("Request contained invalid parameters in {}", path);
      throw ex;
    } catch (de.digitalcollections.iiif.hymir.model.api.exception.UnsupportedFormatException ex) {
      LOGGER.info("Unsupported format ({}) was request in {}", format, path);
      throw ex;
    } catch (TransformationException ex) {
      LOGGER.error("Error during transformation for {}", path, ex);
      throw ex;
    } catch (ResourceNotFoundException ex) {
      LOGGER.info("Could not find image for {}", path);
      throw ex;
    } finally {
      MDC.clear();
    }
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
  @SuppressWarnings("unchecked")
  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = "{identifier}/info.json",
          method = {RequestMethod.GET, RequestMethod.HEAD})
  public ResponseEntity<String> getInfo(@PathVariable String identifier,
          HttpServletRequest request) throws ResolvingException,
          UnsupportedFormatException, UnsupportedOperationException, UnsupportedEncodingException, ResourceNotFoundException {

    try {
      identifier = URLDecoder.decode(identifier, "UTF-8");

      HttpLoggingUtilities.addRequestClientInfoToMDC(request);
      MDC.put("imageId", identifier);

      String baseUrl = getBasePath(request, identifier);
      ImageInfo img = imageService.getImageInfo(identifier);
      JSONObject info = new JSONObject();
      JSONArray profiles = new JSONArray();
      profiles.add(IIIF_COMPLIANCE);
      info.put("@context", IIIF_CONTEXT); // The context document that describes the semantics of the terms used in the document. This must be the URI: http://iiif.io/api/image/2/context.json for version 2.0 of the IIIF Image API.
      info.put("@id", baseUrl); // The Base URI of the image as defined in URI Syntax, including scheme, server, prefix and identifier without a trailing slash.
      info.put("width", img.getWidth()); // The width in pixels of the full image content, given as an integer.
      info.put("height", img.getHeight()); // The height in pixels of the full image content, given as an integer.
      info.put("profile", profiles); // An array of profiles, indicated by either a URI or an object describing the features supported. The first entry in the array must be a compliance level URI, as defined below.
      info.put("protocol", "http://iiif.io/api/image");

      // Add scale factors that are ideal for the TurboJPEG implementation
      JSONArray scaleFactors = new JSONArray();
      Collections.addAll(scaleFactors, 1, 2, 4, 8, 16, 32);

      // Ditto for tiles
      JSONArray tiles = new JSONArray();
      IntStream.of(128, 256, 512)
              .mapToObj(size -> {
                JSONObject tile = new JSONObject();
                tile.put("width", size);
                tile.put("height", size);
                tile.put("scaleFactors", scaleFactors);
                return tile;
              })
              .forEach(tiles::add);
      info.put("tiles", tiles);

      HttpHeaders headers = new HttpHeaders();
      String contentType = request.getHeader("Accept");
      if (contentType != null && contentType.equals("application/ld+json")) {
        headers.set("Content-Type", contentType);
      } else {
        headers.set("Content-Type", "application/json");
        headers.set("Link", "<http://iiif.io/api/image/2/context.json>; "
                    + "rel=\"http://www.w3.org/ns/json-ld#context\"; "
                    + "type=\"application/ld+json\"");
      }
      String json = info.toJSONString();
      LOGGER.info("Serving info.json for image {}", identifier);
      return new ResponseEntity<>(json, headers, HttpStatus.OK);
    } catch (UnsupportedFormatException ex) {
      throw ex;
    } catch (ResourceNotFoundException ex) {
      throw ex;
    } finally {
      MDC.clear();
    }
  }

  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = "{identifier}", method = {RequestMethod.GET, RequestMethod.HEAD})
  public String getInfoRedirect(@PathVariable String identifier) {
    return "redirect:/image/" + VERSION + "/" + identifier + "/info.json";
  }
}
