package de.digitalcollections.iiif.hymir.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import java.util.Arrays;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;

@Configuration
public class SpringConfig {

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

  @Bean
  @Primary
  public IiifObjectMapper objectMapper() {
    IiifObjectMapper objectMapper = new IiifObjectMapper();
    return objectMapper;
  }
}
