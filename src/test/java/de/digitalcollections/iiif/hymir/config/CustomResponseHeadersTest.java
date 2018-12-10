package de.digitalcollections.iiif.hymir.config;

import de.digitalcollections.iiif.hymir.Application;
import de.digitalcollections.iiif.hymir.config.CustomResponseHeaders.ResponseHeader;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        properties = {"spring.profiles.active=TEST",
                      "spring.config.name=application-customResponseHeaders-test"},
        classes = {Application.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomResponseHeadersTest {

  @Autowired
  private CustomResponseHeaders headers;

  @Test
  public void testCustomResponseHeadersConfiguration() {
    assertThat(headers).isNotNull();

    assertThat(headers.forImageTile().size()).isEqualTo(2);

    final List<CustomResponseHeaders.ResponseHeader> headersImageInfo = headers.forImageInfo();
    assertThat(headersImageInfo.size()).isEqualTo(2);
    ResponseHeader header1 = headersImageInfo.get(1);
    assertThat(header1.getName()).isEqualTo("header1");
    assertThat(header1.getValue()).isEqualTo("value1");

    assertThat(headers.forPresentationManifest().size()).isEqualTo(3);
    assertThat(headers.forPresentationCollection().size()).isEqualTo(1);
  }

}
