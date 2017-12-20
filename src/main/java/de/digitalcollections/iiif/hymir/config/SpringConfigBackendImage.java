package de.digitalcollections.iiif.hymir.config;

import de.digitalcollections.core.backend.api.resource.ResourceRepository;
import de.digitalcollections.core.model.api.resource.Resource;
import javax.imageio.ImageIO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Backend configuration.
 */
@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.core.config"
})
public class SpringConfigBackendImage {
  static {
    ImageIO.setUseCache(false);  // Use Heap memory for caching instead of disk
  }
}
