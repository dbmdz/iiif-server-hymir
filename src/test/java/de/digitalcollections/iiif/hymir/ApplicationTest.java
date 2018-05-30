package de.digitalcollections.iiif.hymir;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.web.server.LocalManagementPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * Basic integration tests for webapp endpoints.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // set random webapp/server port
@TestPropertySource(properties = {"management.server.port=0", "spring.profiles.active=PROD"}) // set random management port
public class ApplicationTest {

  @LocalServerPort
  private int port;

  @LocalManagementPort
  private int mgt;

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Test
  public void shouldReturn200WhenSendingRequestToRoot() {
    @SuppressWarnings("rawtypes")
    ResponseEntity<String> entity = this.testRestTemplate.getForEntity(
            "http://localhost:" + this.port + "/", String.class);

    then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void shouldReturn200WhenSendingAuthorizedRequestToSensitiveManagementEndpoint() {
    @SuppressWarnings("rawtypes")
    ResponseEntity<Map> entity = this.testRestTemplate.withBasicAuth("admin", "secret").getForEntity(
            "http://localhost:" + this.mgt + "/monitoring/env", Map.class);

    then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void shouldReturn401WhenSendingUnauthorizedRequestToSensitiveManagementEndpoint() {
    @SuppressWarnings("rawtypes")
    ResponseEntity<Map> entity = this.testRestTemplate.getForEntity(
            "http://localhost:" + this.mgt + "/monitoring/env", Map.class);

    then(entity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

}
