package de.digitalcollections.iiif.hymir.frontend;

import de.digitalcollections.iiif.hymir.config.WebjarProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

  private final Map<String, String> webjarVersions;

  public GlobalControllerAdvice(WebjarProperties webjarProperties) {
    this.webjarVersions = webjarProperties.getVersions();
  }

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP",
      justification = "Value is only used by the Spring framework")
  /** Adds the webjar versions read from yaml files as global model attribute. */
  @ModelAttribute("webjarVersions")
  public Map<String, String> getWebjarVersions() {
    return webjarVersions;
  }
}
