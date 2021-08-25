package de.digitalcollections.iiif.hymir.config;

import static java.util.Collections.emptyMap;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/** Model for the webjar versions in <code>application-webjars.yml</code>. */
@ConfigurationProperties(prefix = "webjars")
@ConstructorBinding
public class WebjarProperties {

  private final Map<String, String> versions;

  public WebjarProperties(Map<String, String> versions) {
    this.versions = (versions != null) ? versions : emptyMap();
  }

  public Map<String, String> getVersions() {
    return Map.copyOf(versions);
  }
}
