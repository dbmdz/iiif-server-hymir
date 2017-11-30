package de.digitalcollections.iiif.hymir.model.exception;

public class InvalidDataException extends Exception {

  public InvalidDataException(String message, Exception e) {
    super(message, e);
  }
}
