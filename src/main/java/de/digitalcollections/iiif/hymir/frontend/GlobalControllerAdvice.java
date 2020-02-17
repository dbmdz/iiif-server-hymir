package de.digitalcollections.iiif.hymir.frontend;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Adds the webjar versions read from yaml files as global model attribute. */
@ControllerAdvice
public class GlobalControllerAdvice {

  @Autowired
  @Value("#{webjarVersions}")
  private Map<String, String> webjarVersions;

  @ModelAttribute("webjarVersions")
  public Map<String, String> getWebjarVersions() {
    return webjarVersions;
  }
}
