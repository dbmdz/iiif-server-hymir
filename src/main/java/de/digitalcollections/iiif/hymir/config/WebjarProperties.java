package de.digitalcollections.iiif.hymir.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Model for the webjar versions in <code>application-webjars.yml</code>. */
@ConfigurationProperties(prefix = "webjars")
public class WebjarProperties {

  private final Map<String, String> versions;

  public WebjarProperties(Map<String, String> versions) {
    this.versions = new HashMap<>(versions);
  }

  @SuppressFBWarnings(
      value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
      justification = "Value is only used by the Spring framework")
  public Map<String, String> getVersions() {
    return versions;
  }
}
