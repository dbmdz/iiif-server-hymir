package de.digitalcollections.iiif.hymir;

import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * Basic integration tests for webapp endpoints.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // set random webapp/server port
@TestPropertySource(properties = {"management.port=0", "spring.profiles.active=PROD"}) // set random management port
public class ApplicationTest {

  // "local" is not profile name, it is needed to use random port
  @Value("${local.server.port}")
  private int port;

  // "local" is not profile name, it is needed to use random port
  @Value("${local.management.port}")
  private int mgt;

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Test
  public void shouldReturn200WhenSendingRequestToRoot() throws Exception {
    @SuppressWarnings("rawtypes")
    ResponseEntity<String> entity = this.testRestTemplate.getForEntity(
            "http://localhost:" + this.port + "/", String.class);

    then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void shouldReturn200WhenSendingAuthorizedRequestToSensitiveManagementEndpoint() throws Exception {
    @SuppressWarnings("rawtypes")
    ResponseEntity<Map> entity = this.testRestTemplate.withBasicAuth("admin", "secret").getForEntity(
            "http://localhost:" + this.mgt + "/monitoring/env", Map.class);

    then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void shouldReturn401WhenSendingUnauthorizedRequestToSensitiveManagementEndpoint() throws Exception {
    @SuppressWarnings("rawtypes")
    ResponseEntity<Map> entity = this.testRestTemplate.getForEntity(
            "http://localhost:" + this.mgt + "/monitoring/env", Map.class);

    then(entity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

}
