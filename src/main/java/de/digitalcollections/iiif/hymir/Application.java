package de.digitalcollections.iiif.hymir;

import de.digitalcollections.iiif.hymir.cli.Cli;
import de.digitalcollections.iiif.hymir.cli.CliException;
import de.digitalcollections.iiif.hymir.cli.ExitStatus;
import java.io.PrintWriter;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.ResourceBanner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.util.UrlPathHelper;

@SpringBootApplication
@EnableAutoConfiguration
public class Application implements WebMvcConfigurer {
  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    // Allow escaped slashes in routes
    System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
    processArguments(args);
    SpringApplicationBuilder builder = new SpringApplicationBuilder(Application.class);
    builder.banner(hymirBanner());
    builder.run(args);
  }

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    // Needed for escaped slashes in identifiers
    UrlPathHelper urlPathHelper = new UrlPathHelper();
    urlPathHelper.setUrlDecode(false);
    configurer.setUrlPathHelper(urlPathHelper);
  }

  private static Banner hymirBanner() {
    Resource resource = new ClassPathResource("banner.txt");
    return new ResourceBanner(resource);
  }

  private static void processArguments(String[] args) {
    Cli cli;
    try {
      cli = new Cli(new PrintWriter(System.out), args);
      if (cli.hasExitStatus()) {
        System.exit(cli.getExitStatus());
      }
      if (cli.hasRulesPath()) {
        System.setProperty("multiPatternResolvingFile", cli.getRulesPath());
      }
      if (cli.hasSpringProfiles()) {
        System.setProperty("spring.profiles.active", cli.getSpringProfiles());
      }
    } catch (CliException e) {
      LOGGER.error(e.getMessage());
      System.exit(ExitStatus.ERROR);
    } catch (ParseException e) {
      LOGGER.error("Could not parse command line arguments", e);
      System.exit(ExitStatus.ERROR);
    }
  }
}
