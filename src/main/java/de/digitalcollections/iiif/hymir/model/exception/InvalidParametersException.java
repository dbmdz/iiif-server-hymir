package de.digitalcollections.iiif.hymir.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidParametersException extends Exception {

  public InvalidParametersException() {}

  public InvalidParametersException(String message) {
    super(message);
  }

  public InvalidParametersException(Exception e) {
    super(e);
  }
}
