package de.digitalcollections.iiif.hymir.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResolvingException extends Exception {

  private static final long serialVersionUID = 1L;

  public ResolvingException() {
    super();
  }

  public ResolvingException(String message) {
    super(message);
  }

  public ResolvingException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public ResolvingException(String message, Throwable cause) {
    super(message, cause);
  }

  public ResolvingException(Throwable cause) {
    super(cause);
  }
}
