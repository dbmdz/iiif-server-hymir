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
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@SpringBootApplication
public class Application {

  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    processArguments(args);
    SpringApplicationBuilder builder = new SpringApplicationBuilder(Application.class);
    builder.banner(hymirBanner());
    builder.run(args);
  }

  private static Banner hymirBanner() {
    Resource resource = new ClassPathResource("banner.txt");
    return new ResourceBanner(resource);
  }

  private static void processArguments(String[] args) {
    Cli cli = null;
    try {
      cli = new Cli(new PrintWriter(System.out), args);
    } catch (CliException e) {
      LOGGER.error(e.getMessage());
      System.exit(ExitStatus.ERROR);
    } catch (ParseException e) {
      LOGGER.error("Could not parse command line arguments", e);
      System.exit(ExitStatus.ERROR);
    }
    if (cli.hasExitStatus()) {
      System.exit(cli.getExitStatus());
    }
    if (cli.hasRulesPath()) {
      System.setProperty("multiPatternResolvingFile", cli.getRulesPath());
    }
    if (cli.hasSpringProfiles()) {
      System.setProperty("spring.profiles.active", cli.getSpringProfiles());
    }
  }

//  @Override
//  public void afterPropertiesSet() throws Exception {
//    if (!StringUtils.isEmpty(rulesPath)) {
//      System.setProperty("multiPatternResolvingFile", rulesPath);
//    }
//  }
}
