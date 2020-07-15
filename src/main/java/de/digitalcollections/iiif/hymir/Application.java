package de.digitalcollections.iiif.hymir;

import org.springframework.boot.Banner;
import org.springframework.boot.ResourceBanner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@ConfigurationPropertiesScan
@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    // Allow escaped slashes in routes
    System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
    SpringApplicationBuilder builder = new SpringApplicationBuilder(Application.class);
    builder.banner(hymirBanner());
    builder.run(args);
  }

  private static Banner hymirBanner() {
    Resource resource = new ClassPathResource("banner.txt");
    return new ResourceBanner(resource);
  }
}
