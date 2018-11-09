package de.digitalcollections.iiif.hymir.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Backend configuration.
 */
@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.commons.file.config"
})
public class SpringConfigBackendPresentation {
}
