package de.digitalcollections.iiif.hymir.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SecurityException extends Exception {

  public SecurityException(String msg) {
    super(msg);
  }
}
