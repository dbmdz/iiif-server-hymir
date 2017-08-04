package de.digitalcollections.iiif.hymir.cli;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

public class CliTest {

  private PrintWriter printWriter;

  private StringWriter stringWriter;

  @Before
  public void setUp() {
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
  }

  @Test
  public void shouldGetRulesPath() throws ParseException, CliException {
    Cli cli = new Cli(printWriter, "--rules=README.md");
    assertThat(cli.getRulesPath()).isEqualTo("README.md");
  }

  @Test
  public void shouldTellIfRulesPathIsSet() throws ParseException, CliException {
    Cli cli = new Cli(printWriter, "--rules=README.md");
    assertThat(cli.hasRulesPath()).isTrue();
  }

  @Test(expected = CliException.class)
  public void shouldThrowExceptionIfRulesPathIsWrong() throws ParseException, CliException {
    new Cli(printWriter, "--rules=doesnothexist.yml");
  }

  @Test
  public void shoulAcceptUriAsRulesPath() throws ParseException, CliException {
    new Cli(printWriter, "--rules=http://www.google.de");
  }

  @Test(expected = CliException.class)
  public void shouldThrowExceptionIfRulesPathIsWrongUrl() throws ParseException, CliException {
    new Cli(printWriter, "--rules=http://www.adjkhaskdjakdhakjsdhjaksdhaksjd-doesnothexist.de");
  }

  @Test
  public void shouldShowHelp() throws ParseException, CliException {
    Cli cli = new Cli(printWriter, "--help");
    assertThat(stringWriter.toString())
        .contains("help")
        .contains("profiles")
        .contains("rulesPath");
  }

  @Test
  public void shouldGetSpringProfiles() throws ParseException, CliException {
    Cli cli = new Cli(printWriter, "-p=dev");
    assertThat(cli)
        .returns(true, from(Cli::hasSpringProfiles))
        .returns("dev", from(Cli::getSpringProfiles));
  }

  @Test
  public void helpShouldSetExitStatusOK() throws ParseException, CliException {
    Cli cli = new Cli(printWriter, "--help");
    assertThat(cli)
        .returns(true, from(Cli::hasExitStatus))
        .returns(ExitStatus.OK, from(Cli::getExitStatus));
  }

}
