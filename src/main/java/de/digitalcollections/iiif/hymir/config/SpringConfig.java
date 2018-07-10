package de.digitalcollections.iiif.hymir.config;

import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@ComponentScan(basePackages = {
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
}
