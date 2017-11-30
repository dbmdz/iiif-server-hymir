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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

/**
 * IIIF Presentation API implementation. Supported URLs (examples):
 * <ul>
 * <li>http://localhost:9898/presentation/v2/1234/manifest</li>
 * </ul>
 */
@Controller(value = "IIIFPresentationApiController-v2")
@RequestMapping("/presentation/v2")
public class IIIFPresentationApiController {

  private static final Logger LOGGER = LoggerFactory.getLogger(IIIFPresentationApiController.class);
  public static final String VERSION = "v2";

  @Autowired
  private PresentationService presentationService;

  /**
   * The manifest response contains sufficient information for the client to initialize itself and begin to display
   * something quickly to the user. The manifest resource represents a single object and any intellectual work or works
   * embodied within that object. In particular it includes the descriptive, rights and linking information for the
   * object. It then embeds the sequence(s) of canvases that should be rendered to the user.
   *
   * @param identifier unique id of object to be shown
   * @param request request containing client information for logging
   * @return the JSON-Manifest
   * @throws ResolvingException if manifest can not be delivered
   * @throws InvalidDataException if manifest can not be read
   * @see <a href="http://iiif.io/api/presentation/2.0/#manifest">IIIF 2.0</a>
   */
  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = {"{identifier}/manifest", "{identifier}"}, method = RequestMethod.GET,
          produces = "application/json")
  @ResponseBody
  public Manifest getManifest(@PathVariable String identifier, HttpServletRequest request) throws ResolvingException, InvalidDataException {
    HttpLoggingUtilities.addRequestClientInfoToMDC(request);
    MDC.put("manifestId", identifier);
    try {
      Manifest manifest = presentationService.getManifest(identifier);
      LOGGER.info("Serving manifest for {}", identifier);
      return manifest;
    } catch (ResolvingException e) {
      LOGGER.info("Did not find manifest for {}", identifier);
      throw e;
    } catch (InvalidDataException e) {
      LOGGER.error("Bad data for {}", identifier);
      throw e;
    } finally {
      MDC.clear();
    }
  }

  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = {"{identifier}/manifest", "{identifier}"}, method = RequestMethod.HEAD)
  @ResponseBody
  public void checkManifest(@PathVariable String identifier, HttpServletResponse response)
          throws ResolvingException, InvalidDataException {
    presentationService.getManifest(identifier);
    response.setStatus(200);
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

  /**
   * Collections are used to list the manifests available for viewing, and to describe the structures, hierarchies or
   * curated collections that the physical objects are part of. The collections may include both other collections and
   * manifests, in order to form a hierarchy of objects with manifests at the leaf nodes of the tree. Collection objects
   * may be embedded inline within other collection objects, such as when the collection is used primarily to subdivide
   * a larger one into more manageable pieces, however manifests must not be embedded within collections. An embedded
   * collection should also have its own URI from which the description is available.
   *
   * The URI pattern follows the same structure as the other resource types, however note that it prevents the existence
   * of a manifest or object with the identifier “collection”. It is also recommended that the topmost collection from
   * which all other collections are discoverable by following links within the heirarchy be named top, if there is one.
   *
   * Manifests or collections may appear within more than one collection. For example, an institution might define four
   * collections: one for modern works, one for historical works, one for newspapers and one for books. The manifest for
   * a modern newspaper would then appear in both the modern collection and the newspaper collection. Alternatively, the
   * institution may choose to have two separate newspaper collections, and reference each as a sub-collection of modern
   * and historical.
   *
   * @param name unique name of collection
   * @param request request containing client information for logging
   * @return the JSON-Collection
   * @throws ResolvingException if collection can not be delivered
   * @throws InvalidDataException if manifest can not be read
   * @see <a href="http://iiif.io/api/presentation/2.1/#collection">IIIF 2.1</a>
   */
  @CrossOrigin(allowedHeaders = {"*"}, origins = {"*"})
  @RequestMapping(value = {"collection/{name}"}, method = {RequestMethod.GET, RequestMethod.HEAD},
          produces = "application/json")
  @ResponseBody
  public Collection getCollection(@PathVariable String name, HttpServletRequest request) throws ResolvingException, InvalidDataException {
    HttpLoggingUtilities.addRequestClientInfoToMDC(request);
    MDC.put("collection name", name);
    try {
      Collection collection = presentationService.getCollection(name);
      LOGGER.info("Serving collection for {}", name);
      return collection;
    } catch (ResolvingException e) {
      LOGGER.info("Did not find collection for {}", name);
      throw e;
    } catch (InvalidDataException e) {
      LOGGER.info("Bad data for {}", name);
      throw e;
    } finally {
      MDC.clear();
    }
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
