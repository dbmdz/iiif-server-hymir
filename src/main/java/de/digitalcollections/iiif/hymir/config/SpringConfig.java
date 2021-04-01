package de.digitalcollections.iiif.hymir.config;

import com.github.dbmdz.pathfinder.Pathfinder;
import de.digitalcollections.iiif.hymir.util.HttpUrlFinder;
import de.digitalcollections.iiif.hymir.util.HttpUrlFinder.UrlSpec;
import de.digitalcollections.iiif.hymir.util.LegacyResolvingProperties;
import de.digitalcollections.iiif.hymir.util.LegacyResolvingProperties.ResolvingSpec;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@ComponentScan(
    basePackages = {
      "de.digitalcollections.commons.springboot.actuator",
      "de.digitalcollections.commons.springboot.contributor",
      "de.digitalcollections.commons.springboot.metrics",
      "de.digitalcollections.commons.springboot.monitoring"
    })
@Configuration
public class SpringConfig {

  @Bean
  @Primary
  public IiifObjectMapper objectMapper() {
    return new IiifObjectMapper();
  }

  @Bean
  Pathfinder pathfinder(LegacyResolvingProperties resolvingProperties) {
    Pathfinder finder = new Pathfinder();
    for (ResolvingSpec spec : resolvingProperties.getPathSpecs()) {
      finder.addPattern(spec.pattern, spec.substitutions.toArray(String[]::new));
    }
    return finder;
  }

  @Bean
  HttpUrlFinder urlFinder(LegacyResolvingProperties resolvingProperties) {
    return new HttpUrlFinder(
        resolvingProperties.getUrlSpecs().stream()
        .map(spec -> new UrlSpec(spec.pattern, spec.substitutions.get((0))))
        .collect(Collectors.toList())
    );
  }
}
