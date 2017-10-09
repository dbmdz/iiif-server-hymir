package de.digitalcollections.iiif.hymir.image.model.api.v2;

public class TransformationException extends Exception {

  public TransformationException() {
  }

  public TransformationException(String message) {
    super(message);
  }

  public TransformationException(String message, Throwable cause) {
    super(message, cause);
  }

  public TransformationException(Throwable cause) {
    super(cause);
  }

  public TransformationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
