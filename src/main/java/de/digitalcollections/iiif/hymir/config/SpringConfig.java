package de.digitalcollections.iiif.hymir.config;

import java.util.Arrays;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;

@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.iiif.image.config",
  "de.digitalcollections.iiif.presentation.config"
}) // scans all frontend, business and backend configs of Image API and Presentation API
public class SpringConfig implements EnvironmentAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfig.class);

  /**
   * Create a resource bundle for your messages ("messages.properties").<br>
   * This file goes in src/main/resources because you want it to appear at the root of the classpath on deployment.
   *
   * @return message source
   */
  @Bean(name = "messageSource")
  public MessageSource configureMessageSource() {
    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setBasenames("classpath:messages", "classpath:messages-overlay", "classpath:messages-commons");
    messageSource.setCacheSeconds(5);
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
  }

  @Override
  public void setEnvironment(Environment environment) {
    String[] activeProfiles = environment.getActiveProfiles();
    String activeProfilesStr = Arrays.toString(activeProfiles);
    LOGGER.info("##### Active Profiles: " + activeProfilesStr);

    Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("JPEG");
    while (readers.hasNext()) {
      LOGGER.info("##### ImageIO reader: " + readers.next());
    }
    readers = ImageIO.getImageReadersByFormatName("TIFF");
    while (readers.hasNext()) {
      LOGGER.info("##### ImageIO reader: " + readers.next());
    }
  }
}
