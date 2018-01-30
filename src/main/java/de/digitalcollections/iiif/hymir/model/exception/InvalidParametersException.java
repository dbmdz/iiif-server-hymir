package de.digitalcollections.iiif.hymir.model.exception;

public class InvalidParametersException extends Exception {

  public InvalidParametersException() {
  }

  public InvalidParametersException(String message) {
    super(message);
  }

  public InvalidParametersException(Exception e) {
    super(e);
  }
}
