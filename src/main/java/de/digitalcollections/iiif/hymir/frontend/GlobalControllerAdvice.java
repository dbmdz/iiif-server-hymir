package de.digitalcollections.iiif.hymir.frontend;

import static java.util.Collections.emptyMap;

import de.digitalcollections.iiif.hymir.config.WebjarProperties;
import java.util.Map;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

  private final Map<String, String> webjarVersions;

  public GlobalControllerAdvice(WebjarProperties webjarProperties) {
    var webjarVersions = webjarProperties.getVersions();
    this.webjarVersions = (webjarVersions != null) ? webjarVersions : emptyMap();
  }

  /** Adds the webjar versions read from yaml files as global model attribute. */
  @ModelAttribute("webjarVersions")
  public Map<String, String> getWebjarVersions() {
    return Map.copyOf(webjarVersions);
  }
}
