package de.digitalcollections.iiif.hymir.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InvalidDataException extends Exception {

  public InvalidDataException(String message, Exception e) {
    super(message, e);
  }
}
