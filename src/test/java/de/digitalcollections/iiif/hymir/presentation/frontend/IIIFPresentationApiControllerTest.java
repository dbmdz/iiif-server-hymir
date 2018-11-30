package de.digitalcollections.iiif.hymir.presentation.frontend;

import de.digitalcollections.iiif.hymir.Application;
import de.digitalcollections.iiif.hymir.TestConfiguration;
import de.digitalcollections.iiif.hymir.presentation.business.PresentationServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class, TestConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IIIFPresentationApiControllerTest {

  @LocalServerPort
  int randomServerPort;

  @Autowired
  protected IIIFPresentationApiController iiifController;

  @Autowired
  protected PresentationServiceImpl presentationService;

  @Autowired
  private TestRestTemplate restTemplate;

  @BeforeAll
  public static void beforeAll() {
    System.setProperty("spring.profiles.active", "TEST");
    System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
    TestConfiguration.setDefaults();
  }

  @Test
  public void testInvalidDataInManifest() {
    ResponseEntity<String> response = restTemplate.getForEntity("/presentation/" + IIIFPresentationApiController.VERSION + "/manifest-invalid-data/manifest", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
