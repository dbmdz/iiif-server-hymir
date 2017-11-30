package de.digitalcollections.iiif.hymir.frontend.impl.springmvc.controller;

import de.digitalcollections.iiif.hymir.presentation.frontend.impl.springmvc.controller.v2.IIIFPresentationApiController;
import java.util.Map;
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

  @Autowired
  @Value("#{iiifVersions}")
  private Map<String, String> iiifVersions;

  @RequestMapping(value = "/presentation/{identifier}/view.html", method = RequestMethod.GET)
  public String viewExtendedPresentationGet(@PathVariable String identifier, Model model) {
    model.addAttribute("iiifVersions", iiifVersions);
    model.addAttribute("presentationUri", "/presentation/" + IIIFPresentationApiController.VERSION + "/" + identifier);
    return "mirador/view";
  }
}
