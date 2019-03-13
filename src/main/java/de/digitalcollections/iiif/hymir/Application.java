package de.digitalcollections.iiif.hymir;

import de.digitalcollections.iiif.hymir.cli.Cli;
import de.digitalcollections.iiif.hymir.cli.CliException;
import de.digitalcollections.iiif.hymir.cli.ExitStatus;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.PrintWriter;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.ResourceBanner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@SpringBootApplication
public class Application {

  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    // Allow escaped slashes in routes
    System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
    processArguments(args);
    SpringApplicationBuilder builder = new SpringApplicationBuilder(Application.class);
    builder.banner(hymirBanner());
    builder.run(args);
  }

  private static Banner hymirBanner() {
    Resource resource = new ClassPathResource("banner.txt");
    return new ResourceBanner(resource);
  }

  @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING", justification = "The PrintWriter constructor with charset is not supported in Java 8")
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
