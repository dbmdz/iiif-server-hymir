package de.digitalcollections.iiif.hymir.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsupportedFormatException extends Exception {

  public UnsupportedFormatException(String message) {
    super(message);
  }

  public UnsupportedFormatException() {
    super();
  }
}
