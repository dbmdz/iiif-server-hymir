package de.digitalcollections.iiif.hymir.frontend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ApiErrorTest {

  @Test
  void getReason() {
    String expected = "expected reason";
    ApiError apiError = new ApiError(new Exception(expected));
    assertThat(apiError.getReason()).isEqualTo(expected);
  }
}
