package de.digitalcollections.iiif.hymir.config;

import javax.imageio.ImageIO;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Backend configuration.
 */
@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.commons.file.config"
})
public class SpringConfigBackendImage {

  static {
    ImageIO.setUseCache(false);  // Use Heap memory for caching instead of disk
  }
}
