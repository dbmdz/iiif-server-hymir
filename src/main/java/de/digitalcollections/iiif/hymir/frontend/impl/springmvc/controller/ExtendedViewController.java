package de.digitalcollections.iiif.hymir.frontend.impl.springmvc.controller;

import de.digitalcollections.commons.server.HttpLoggingUtilities;
import de.digitalcollections.iiif.hymir.model.api.exception.InvalidDataException;
import de.digitalcollections.iiif.hymir.model.api.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.presentation.business.api.v2.PresentationService;
import de.digitalcollections.iiif.hymir.presentation.frontend.impl.springmvc.controller.v2.IIIFPresentationApiController;
import java.net.URI;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for serving viewer page.
 *
 * Provides direct access to viewer for external call. Can be overwritten with custom behaviour.
 */
@Controller
public class ExtendedViewController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedViewController.class);

  @Autowired
  @Value("#{iiifVersions}")
  private Map<String, String> iiifVersions;

  @Autowired
  private PresentationService presentationService;

  @RequestMapping(value = "/presentation/{identifier}/view.html", method = RequestMethod.GET)
  public String viewExtendedPresentationGet(@PathVariable String identifier, Model model) {
    model.addAttribute("iiifVersions", iiifVersions);
    model.addAttribute("presentationUri", "/presentation/" + IIIFPresentationApiController.VERSION + "/" + identifier);
    return "mirador/view";
  }

  /**
   * Direct link for viewing a specified canvas (page) used for citation.
   * https://api.digitale-sammlungen.de/iiif/presentation/v2/bsb00107186/canvas/1
   * @param version api version
   * @param objectIdentifier object identifier
   * @param canvasName name of canvas
   * @param model mvc model
   * @param request request
   * @return canvas specific view
   * @throws ResolvingException if no manifest found
   * @throws InvalidDataException if manifest can't be read
   */
  @RequestMapping(value = "/presentation/{version}/{objectIdentifier}/canvas/{canvasName}/view", method = RequestMethod.GET)
  public String viewCanvasGet(@PathVariable String version, @PathVariable String objectIdentifier, @PathVariable String canvasName, Model model, HttpServletRequest request)
          throws ResolvingException, InvalidDataException {
    HttpLoggingUtilities.addRequestClientInfoToMDC(request);
    MDC.put("manifestId", objectIdentifier);
    MDC.put("canvasName", canvasName);

    String url = getOriginalUri(request).toString();
    String canvasId = url.substring(0, url.indexOf("/view"));
    String manifestId = url.substring(0, url.indexOf("/canvas")) + "/manifest";

    try {
      presentationService.getCanvas(objectIdentifier, canvasId);
      LOGGER.info("Serving Canvas for {}", canvasId);

      model.addAttribute("manifestId", manifestId);
      model.addAttribute("canvasId", canvasId);

    } catch (ResolvingException e) {
      LOGGER.info("Did not find canvas for {}", canvasId);
      throw e;
    } catch (InvalidDataException e) {
      LOGGER.error("Bad data for {}", objectIdentifier);
      throw e;
    } finally {
      MDC.clear();
    }

    model.addAttribute("iiifVersions", iiifVersions);
    return "mirador/viewCanvas";
  }

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