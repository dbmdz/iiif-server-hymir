package de.digitalcollections.iiif.hymir.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Model for the webjar versions in <code>application-webjars.yml</code>. */
@ConfigurationProperties(prefix = "webjars")
public class WebjarProperties {

  private final Map<String, String> versions;

  public WebjarProperties(Map<String, String> versions) {
    this.versions = versions;
  }

  public Map<String, String> getVersions() {
    return versions;
  }
}
