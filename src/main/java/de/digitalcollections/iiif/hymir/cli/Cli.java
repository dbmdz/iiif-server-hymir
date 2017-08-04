package de.digitalcollections.iiif.hymir.cli;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Cli {

  private final PrintWriter out;

  private String rulesPath = null;

  private int exitStatus = -1;

  private String springProfiles = null;

  public Cli(PrintWriter out, String... args) throws ParseException, CliException {
    this.out = out;
    Options options = new Options();
    options.addOption("h", "help", false, "Show help");
    options.addOption("r", "rulesPath", true, "The resolving rulesPath to map names to images or manifests");
    options.addOption("p", "spring.profiles.active", true, "The active spring configuration");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    if (cmd.hasOption("help")) {
      showHelp(options);
      exitStatus = ExitStatus.OK;
      return;
    }

    if (cmd.hasOption("rulesPath")) {
      rulesPath = cmd.getOptionValue("rulesPath");
      validateRulesPath();
    }

    if (cmd.hasOption("spring.profiles.active")) {
      springProfiles = cmd.getOptionValue("spring.profiles.active");
    }

  }

  private void validateRulesPath() throws CliException {
    if (rulesPath.contains(":")) {
      try {
        URL url = new URL(rulesPath);
        url.getContent();
      } catch (MalformedURLException e) {
        throw new CliException("Cannot parse URL: " + rulesPath);
      } catch (IOException e) {
        throw new CliException("Rules configuration not found: " + rulesPath);
      }
    } else {
      File rulesFile = new File(rulesPath);
      if (!rulesFile.exists()) {
        throw new CliException("Rules configuration not found: " + rulesPath);
      }
    }
  }

  public boolean hasRulesPath() {
    return rulesPath != null;
  }

  public String getRulesPath() {
    return rulesPath;
  }

  public boolean hasExitStatus() {
    return exitStatus > -1;
  }

  public int getExitStatus() {
    return exitStatus;
  }

  private void showHelp(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(out, 120, "java -jar hymir-<version>-exec.jar", null, options, 2, 8, null, true);
    out.flush();
  }

  public boolean hasSpringProfiles() {
    return springProfiles != null;
  }

  public String getSpringProfiles() {
    return springProfiles;
  }

}
