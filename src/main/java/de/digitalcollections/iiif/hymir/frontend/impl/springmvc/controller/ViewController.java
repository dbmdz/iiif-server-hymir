package de.digitalcollections.iiif.hymir.frontend.impl.springmvc.controller;

import de.digitalcollections.iiif.image.frontend.impl.springmvc.controller.v2.IIIFImageApiController;
import de.digitalcollections.iiif.presentation.frontend.impl.springmvc.controller.v2.IIIFPresentationApiController;
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

  @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
  public String viewHomepage() {
    return "index";
  }

  @RequestMapping(value = "/iiif/image/{identifier}/view.html", method = RequestMethod.GET)
  public String viewImageGet(@PathVariable String identifier, Model model) {
    model.addAttribute("infoUrl", "/image/" + IIIFImageApiController.VERSION + "/" + identifier + "/info.json");
    return "openseadragon/view";
  }

  @RequestMapping(value = "/iiif/image/view", method = RequestMethod.POST)
  public String viewImagePost(@RequestParam String identifier) {
    return "redirect:/iiif/image/" + identifier + "/view.html";
  }

  @RequestMapping(value = "/iiif/presentation/{identifier}/view.html", method = RequestMethod.GET)
  public String viewPresentationGet(@PathVariable String identifier, Model model) {
    model.
            addAttribute("presentationUri", "/presentation/" + IIIFPresentationApiController.VERSION + "/" + identifier);
    return "mirador/view";
  }

  @RequestMapping(value = "/iiif/presentation/view", method = RequestMethod.POST)
  public String viewPresentationPost(@RequestParam String identifier, Model model) {
    return "redirect:/iiif/presentation/" + identifier + "/view.html";
  }

  @RequestMapping(value = "/iiif/presentation/manifest", method = RequestMethod.GET)
  public String viewPresentationManifest(@RequestParam String identifier) {
    return "forward:/presentation/" + IIIFPresentationApiController.VERSION + "/" + identifier + "/manifest";
  }
}
