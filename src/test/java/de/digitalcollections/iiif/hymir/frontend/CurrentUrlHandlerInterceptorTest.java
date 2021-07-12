package de.digitalcollections.iiif.hymir.frontend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CurrentUrlHandlerInterceptorTest {

  @ParameterizedTest
  @CsvSource({
    "language=de,http://host",
    "a=b&language=de,http://host?a=b",
    "language=de&a=b,http://host?a=b"
  })
  void shouldDeleteLanguageParam(String queryString, String expected) {
    CurrentUrlHandlerInterceptor interceptor = new CurrentUrlHandlerInterceptor();

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("language")).thenReturn("de");
    when(request.getQueryString()).thenReturn(queryString);
    when(request.getRequestURI()).thenReturn("http://host");
    when(request.getContextPath()).thenReturn("/context");

    String actual = interceptor.getCurrentUrl(request);
    assertThat(actual).isEqualTo(expected);
  }
}
