package de.digitalcollections.iiif.hymir.frontend.impl.springmvc.controller;

import de.digitalcollections.commons.server.HttpLoggingUtilities;
import de.digitalcollections.iiif.image.frontend.impl.springmvc.controller.v2.IIIFImageApiController;
import de.digitalcollections.iiif.presentation.business.api.v2.PresentationService;
import de.digitalcollections.iiif.presentation.frontend.impl.springmvc.controller.v2.IIIFPresentationApiController;
import de.digitalcollections.iiif.presentation.model.api.exceptions.InvalidDataException;
import de.digitalcollections.iiif.presentation.model.api.exceptions.NotFoundException;
import de.digitalcollections.iiif.presentation.model.api.v2.Canvas;
import javax.servlet.http.HttpServletRequest;
import net.logstash.logback.marker.LogstashMarker;
import static net.logstash.logback.marker.Markers.append;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for serving different view pages.
 */
@Controller
public class ViewController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ViewController.class);

  @Autowired
  private PresentationService presentationService;

  @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
  public String viewHomepage(Model model) {
    model.addAttribute("menu", "home");
    return "index";
  }

  @RequestMapping(value = "/image/{identifier}/view.html", method = RequestMethod.GET)
  public String viewImageGet(@PathVariable String identifier, Model model) {
    model.
            addAttribute("infoUrl", "/image/" + IIIFImageApiController.VERSION + "/" + identifier + "/info.json");
    return "openseadragon/view";
  }

  @RequestMapping(value = "/image/view", method = RequestMethod.POST)
  public String viewImagePost(@RequestParam String identifier) {
    return "redirect:/image/" + identifier + "/view.html";
  }

  @RequestMapping(value = "/presentation/{identifier}/view.html", method = RequestMethod.GET)
  public String viewPresentationGet(@PathVariable String identifier, Model model) {
    model.
            addAttribute("presentationUri", "/presentation/" + IIIFPresentationApiController.VERSION + "/" + identifier);
    return "mirador/view";
  }

  @RequestMapping(value = "/presentation/view", method = RequestMethod.POST)
  public String viewPresentationPost(@RequestParam String identifier, Model model) {
    return "redirect:/presentation/" + identifier + "/view.html";
  }

  @RequestMapping(value = "/presentation/manifest", method = RequestMethod.GET)
  public String viewPresentationManifest(@RequestParam String identifier) {
    return "forward:/presentation/" + IIIFPresentationApiController.VERSION + "/" + identifier + "/manifest";
  }

  @RequestMapping(value = "/presentation/collection", method = RequestMethod.GET)
  public String viewPresentationCollection(@RequestParam String name) {
    return "forward:/presentation/" + IIIFPresentationApiController.VERSION + "/collection/" + name;
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
   * @throws de.digitalcollections.iiif.presentation.model.api.exceptions.NotFoundException
   * @throws de.digitalcollections.iiif.presentation.model.api.exceptions.InvalidDataException
   */
  @RequestMapping(value = "/presentation/{version}/{objectIdentifier}/canvas/{canvasName}/view",
          method = RequestMethod.GET)
  public String viewCanvasGet(@PathVariable String version, @PathVariable String objectIdentifier, @PathVariable String canvasName, Model model, HttpServletRequest request)
          throws NotFoundException, InvalidDataException {
    LogstashMarker marker = HttpLoggingUtilities.makeRequestLoggingMarker(request)
            .and(append("manifestId", objectIdentifier).and(append("canvasName", canvasName)));
    try {
      String url = request.getRequestURL().toString();
      String canvasId = url.substring(0, url.indexOf("/view"));
      String manifestId = url.substring(0, url.indexOf("/canvas")) + "/manifest";

      Canvas canvas = presentationService.getCanvas(objectIdentifier, canvasId);
      LOGGER.info(marker, "Serving Canvas for {}", canvasId);

      model.addAttribute("manifestId", manifestId);
      model.addAttribute("canvasId", canvasId);

    } catch (NotFoundException e) {
      LOGGER.info(marker, "Did not find manifest for {}", objectIdentifier);
      throw e;
    } catch (InvalidDataException e) {
      LOGGER.error(marker, "Bad data for {}", objectIdentifier);
      throw e;
    }

    return "mirador/viewCanvas";
  }
}
