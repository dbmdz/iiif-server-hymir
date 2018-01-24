package de.digitalcollections.iiif.hymir.config;

import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SpringConfig {
  @Bean
  @Primary
  public IiifObjectMapper objectMapper() {
    return new IiifObjectMapper();
  }
}
