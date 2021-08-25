package de.digitalcollections.iiif.hymir.config;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.iiif.hymir.Application;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    properties = {"spring.profiles.active=TEST", "spring.config.name=application-test"},
    classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebjarPropertiesTest {

  @Autowired WebjarProperties webjarProperties;

  @Test
  void testProperties() {
    Map<String, String> versions = webjarProperties.getVersions();
    assertThat(versions).isEqualTo(Map.of("bootstrap", "1.2.3", "html5shiv", "4.5.6"));
  }
}
