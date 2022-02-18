package de.digitalcollections.iiif.hymir.frontend;

import de.digitalcollections.iiif.hymir.image.frontend.IIIFImageApiController;
import de.digitalcollections.iiif.hymir.presentation.frontend.IIIFPresentationApiController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/** Controller for serving different view pages. */
@Deprecated(forRemoval = true)  // Will be gone with the next major version
@Controller
public class ViewController {

  @RequestMapping(
      value = {"", "/"},
      method = RequestMethod.GET)
  public String viewHomepage(Model model) {
    model.addAttribute("menu", "home");
    return "index";
  }

  @RequestMapping(value = "/image/{identifier}/view.html", method = RequestMethod.GET)
  public String viewImageGet(@PathVariable String identifier, Model model) {
    model.addAttribute(
        "infoUrl", "/image/" + IIIFImageApiController.VERSION + "/" + identifier + "/info.json");
    return "openseadragon/view";
  }

  @RequestMapping(value = "/image/view", method = RequestMethod.POST)
  public String viewImagePost(@RequestParam String identifier) {
    return "redirect:/image/" + identifier + "/view.html";
  }

  @RequestMapping(value = "/presentation/view", method = RequestMethod.POST)
  public String viewPresentationPost(@RequestParam String identifier, Model model) {
    return "redirect:/presentation/view/" + identifier;
  }

  @RequestMapping(value = "/presentation/view/{identifier}", method = RequestMethod.GET)
  public String viewPresentationGet(@PathVariable String identifier, Model model) {
    model.addAttribute(
        "presentationUri",
        "/presentation/" + IIIFPresentationApiController.VERSION + "/" + identifier);
    return "mirador/view";
  }

  @RequestMapping(value = "/presentation/manifest", method = RequestMethod.GET)
  public String viewPresentationManifest(@RequestParam String identifier) {
    return "redirect:/presentation/"
        + IIIFPresentationApiController.VERSION
        + "/"
        + identifier
        + "/manifest";
  }

  @RequestMapping(value = "/presentation/collection", method = RequestMethod.GET)
  public String viewPresentationCollection(@RequestParam String name) {
    return "redirect:/presentation/"
        + IIIFPresentationApiController.VERSION
        + "/collection/"
        + name;
  }
}
