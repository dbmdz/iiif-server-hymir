package de.digitalcollections.iiif.hymir.presentation.frontend;

import de.digitalcollections.iiif.hymir.config.CustomResponseHeaders;
import de.digitalcollections.iiif.hymir.model.exception.InvalidDataException;
import de.digitalcollections.iiif.hymir.model.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.presentation.business.api.PresentationService;
import de.digitalcollections.iiif.hymir.util.UrlRules;
import de.digitalcollections.iiif.model.sharedcanvas.AnnotationList;
import de.digitalcollections.iiif.model.sharedcanvas.Canvas;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import de.digitalcollections.iiif.model.sharedcanvas.Range;
import de.digitalcollections.iiif.model.sharedcanvas.Sequence;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import java.net.URI;
import java.time.Instant;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping("${custom.iiif.presentation.urlPrefix:/presentation/v2}")
@Deprecated(forRemoval = true)  // Will be gone with the next major version
public class IIIFPresentationApiController {

  public static final String VERSION = "v2";

  @Autowired protected CustomResponseHeaders customResponseHeaders;

  @Autowired private PresentationService presentationService;

  // We set the header ourselves, since using @CrossOrigin doesn't expose "*", but always sets the
  // requesting domain
  // @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(
      value = {"{identifier}/manifest", "{identifier}"},
      method = RequestMethod.GET,
      produces = "application/json")
  @ResponseBody
  public Manifest getManifest(
      @PathVariable String identifier, WebRequest request, HttpServletResponse resp)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    if (UrlRules.isInsecure(identifier)) {
      resp.setStatus(400);
      return null;
    }
    // Return 304 if the manifest has seen no modifications since the requested time
    long modified = presentationService.getManifestModificationDate(identifier).toEpochMilli();
    if (request.checkNotModified(modified)) {
      return null;
    }
    Manifest manifest = presentationService.getManifest(identifier);
    resp.setDateHeader("Last-Modified", modified);
    resp.addHeader("Access-Control-Allow-Origin", "*");

    customResponseHeaders
        .forPresentationManifest()
        .forEach(
            customResponseHeader -> {
              resp.setHeader(customResponseHeader.getName(), customResponseHeader.getValue());
            });
    return manifest;
  }

  @RequestMapping(
      value = {"{identifier}/manifest", "{identifier}"},
      method = RequestMethod.HEAD)
  public void checkManifest(@PathVariable String identifier, HttpServletResponse resp)
      throws ResolvingException, ResourceNotFoundException {
    if (UrlRules.isInsecure(identifier)) {
      resp.setStatus(400);
      return;
    }
    Instant modDate = presentationService.getManifestModificationDate(identifier);
    resp.setDateHeader("Last-Modified", modDate.toEpochMilli());
    resp.addHeader("Access-Control-Allow-Origin", "*");

    customResponseHeaders
        .forPresentationManifest()
        .forEach(
            customResponseHeader -> {
              resp.setHeader(customResponseHeader.getName(), customResponseHeader.getValue());
            });
    resp.setStatus(HttpServletResponse.SC_OK);
  }

  @RequestMapping(
      value = {"{manifestId}/canvas/{canvasId}"},
      method = RequestMethod.GET)
  @ResponseBody
  public Canvas getCanvas(
      @PathVariable String manifestId,
      @PathVariable String canvasId,
      HttpServletRequest req,
      HttpServletResponse resp)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    if (UrlRules.anyIsInsecure(manifestId, canvasId)) {
      resp.setStatus(400);
      return null;
    }
    resp.addHeader("Access-Control-Allow-Origin", "*");

    customResponseHeaders
        .forPresentationManifest()
        .forEach(
            customResponseHeader -> {
              resp.setHeader(customResponseHeader.getName(), customResponseHeader.getValue());
            });
    return presentationService.getCanvas(manifestId, getOriginalUri(req));
  }

  @RequestMapping(
      value = {"{manifestId}/range/{rangeId}"},
      method = RequestMethod.GET)
  @ResponseBody
  public Range getRange(
      @PathVariable String manifestId,
      @PathVariable String rangeId,
      HttpServletRequest req,
      HttpServletResponse resp)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    if (UrlRules.anyIsInsecure(manifestId, rangeId)) {
      resp.setStatus(400);
      return null;
    }
    resp.addHeader("Access-Control-Allow-Origin", "*");

    customResponseHeaders
        .forPresentationManifest()
        .forEach(
            customResponseHeader -> {
              resp.setHeader(customResponseHeader.getName(), customResponseHeader.getValue());
            });
    return presentationService.getRange(manifestId, getOriginalUri(req));
  }

  @RequestMapping(
      value = {"{manifestId}/sequence/{sequenceId}"},
      method = RequestMethod.GET)
  @ResponseBody
  public Sequence getSequence(
      @PathVariable String manifestId,
      @PathVariable String sequenceId,
      HttpServletRequest req,
      HttpServletResponse resp)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    if (UrlRules.anyIsInsecure(manifestId, sequenceId)) {
      resp.setStatus(400);
      return null;
    }
    resp.addHeader("Access-Control-Allow-Origin", "*");

    customResponseHeaders
        .forPresentationManifest()
        .forEach(
            customResponseHeader -> {
              resp.setHeader(customResponseHeader.getName(), customResponseHeader.getValue());
            });
    return presentationService.getSequence(manifestId, getOriginalUri(req));
  }

  @RequestMapping(
      value = {"collection/{identifier}"},
      method = {RequestMethod.GET, RequestMethod.HEAD},
      produces = "application/json")
  @ResponseBody
  public Collection getCollection(
      @PathVariable String identifier, WebRequest request, HttpServletResponse resp)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    if (UrlRules.isInsecure(identifier)) {
      resp.setStatus(400);
      return null;
    }
    long modified = presentationService.getCollectionModificationDate(identifier).toEpochMilli();
    resp.addHeader("Access-Control-Allow-Origin", "*");

    customResponseHeaders
        .forPresentationCollection()
        .forEach(
            customResponseHeader -> {
              resp.setHeader(customResponseHeader.getName(), customResponseHeader.getValue());
            });
    if (request.checkNotModified(modified)) {
      return null;
    }
    Collection collection = presentationService.getCollection(identifier);
    return collection;
  }

  @GetMapping(
      value = {"{identifier}/list/{name}/{canvasId}"},
      produces = "application/json")
  @ResponseBody
  public AnnotationList getAnnotationList(
      @PathVariable String identifier,
      @PathVariable String name,
      @PathVariable String canvasId,
      HttpServletResponse resp)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    if (UrlRules.anyIsInsecure(identifier, name, canvasId)) {
      resp.setStatus(400);
      return null;
    }
    resp.addHeader("Access-Control-Allow-Origin", "*");

    customResponseHeaders
        .forPresentationAnnotationList()
        .forEach(
            customResponseHeader -> {
              resp.setHeader(customResponseHeader.getName(), customResponseHeader.getValue());
            });

    return presentationService.getAnnotationList(identifier, name, canvasId);
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
