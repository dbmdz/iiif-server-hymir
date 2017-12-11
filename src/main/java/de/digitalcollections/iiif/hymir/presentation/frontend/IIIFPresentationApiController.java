package de.digitalcollections.iiif.hymir.presentation.frontend;

import de.digitalcollections.commons.server.HttpLoggingUtilities;
import de.digitalcollections.iiif.hymir.model.exception.InvalidDataException;
import de.digitalcollections.iiif.hymir.model.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.presentation.business.api.PresentationService;
import de.digitalcollections.iiif.model.sharedcanvas.Canvas;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import de.digitalcollections.iiif.model.sharedcanvas.Range;
import de.digitalcollections.iiif.model.sharedcanvas.Sequence;
import java.net.URI;
import java.time.Instant;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping("/presentation/v2")
public class IIIFPresentationApiController {
  private static final Logger LOGGER = LoggerFactory.getLogger(IIIFPresentationApiController.class);
  public static final String VERSION = "v2";

  @Autowired
  private PresentationService presentationService;

  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = {"{identifier}/manifest", "{identifier}"}, method = RequestMethod.GET,
                  produces = "application/json")
  @ResponseBody
  public Manifest getManifest(@PathVariable String identifier, WebRequest request, HttpServletResponse resp)
      throws ResolvingException, InvalidDataException {
    // Return 304 if the manifest has seen no modifications since the requested time
    long modified = presentationService.getManifestModificationDate(identifier).toEpochMilli();
    request.checkNotModified(modified);
    Manifest manifest = presentationService.getManifest(identifier);
    resp.setDateHeader("Last-Modified", modified);
    LOGGER.info("Serving manifest for {}", identifier);
    return manifest;
  }

  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = {"{identifier}/manifest", "{identifier}"}, method = RequestMethod.HEAD)
  public void checkManifest(@PathVariable String identifier, HttpServletResponse resp)
          throws ResolvingException, InvalidDataException {
    Instant modDate = presentationService.getManifestModificationDate(identifier);
    resp.setDateHeader("Last-Modified", modDate.toEpochMilli());
    resp.setStatus(HttpStatus.SC_OK);
  }

  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = {"{manifestId}/canvas/{canvasId}"}, method = RequestMethod.GET)
  @ResponseBody
  public Canvas getCanvas(@PathVariable String manifestId, @PathVariable String canvasId, HttpServletRequest req)
          throws ResolvingException, InvalidDataException {
    return presentationService.getCanvas(manifestId, getOriginalUri(req));
  }

  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = {"{manifestId}/range/{rangeId}"}, method = RequestMethod.GET)
  @ResponseBody
  public Range getRange(@PathVariable String manifestId, @PathVariable String rangeId, HttpServletRequest req)
          throws ResolvingException, InvalidDataException {
    return presentationService.getRange(manifestId, getOriginalUri(req));
  }

  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = {"{manifestId}/sequence/{sequenceId}"}, method = RequestMethod.GET)
  @ResponseBody
  public Sequence getSequence(@PathVariable String manifestId, @PathVariable String sequenceId, HttpServletRequest req)
          throws ResolvingException, InvalidDataException {
    return presentationService.getSequence(manifestId, getOriginalUri(req));
  }

  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = {"collection/{identifier}"}, method = {RequestMethod.GET, RequestMethod.HEAD},
          produces = "application/json")
  @ResponseBody
  public Collection getCollection(@PathVariable String identifier, WebRequest request, HttpServletResponse resp)
      throws ResolvingException, InvalidDataException {
    long modified = presentationService.getCollectionModificationDate(identifier).toEpochMilli();
    request.checkNotModified(modified);
    Collection collection = presentationService.getCollection(identifier);
    LOGGER.info("Serving collection for {}", identifier);
    return collection;
  }

  /**
   * Return the URL as it was originally received from a possible frontend proxy.
   *
   * @param request Incoming request
   * @return URL as it was received at the frontend proxy
   */
  private URI getOriginalUri(HttpServletRequest request) {
    String requestUrl = request.getRequestURL().toString();
    String incomingScheme = URI.create(requestUrl).getScheme();
    String originalScheme = request.getHeader("X-Forwarded-Proto");
    if (originalScheme != null && !incomingScheme.equals(originalScheme)) {
      return URI.create(requestUrl.replaceFirst("^" + incomingScheme, originalScheme));
    } else {
      return URI.create(requestUrl);
    }
  }
}
