package de.digitalcollections.iiif.hymir.frontend;

import de.digitalcollections.iiif.hymir.model.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.model.exception.ScalingException;
import de.digitalcollections.iiif.hymir.model.exception.UnsupportedFormatException;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionAdvice {

  private static final Logger log = LoggerFactory.getLogger(ExceptionAdvice.class);

  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(InvalidParametersException.class)
  public void handleInvalidParametersException(Exception exception) {
    // NOP
  }

  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  @ExceptionHandler(ResolvingException.class)
  public void handleResolvingException(Exception exception) {
    // NOP
  }

  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  @ExceptionHandler(ResourceNotFoundException.class)
  public void handleResourceNotFoundException(Exception exception) {
    // NOP
  }

  /**
   * Sometimes scaling an image can lead to broken dimensions (usually because of rounding to 0).
   *
   * @param exception The exception to handle
   * @return An error description for the client
   */
  @ExceptionHandler(ScalingException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  protected ApiError handleScalingException(ScalingException exception) {
    return new ApiError(exception);
  }

  @ResponseStatus(value = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
  @ExceptionHandler(UnsupportedFormatException.class)
  public void handleUnsupportedFormatException(Exception exception) {
    // NOP
  }

  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(UnsupportedOperationException.class)
  public void handleUnsupportedOperationException(Exception exception) {
    // NOP
  }

  @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public void handleHttpMediaTypeNotAcceptableException(Exception exception) {
    // NOP
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public void handleAllOther(Exception exception) {
    if (exception.getMessage() != null) {
      log.error(exception.getMessage(), exception);
    } else {
      log.error("Unhandled exception during request handling", exception);
    }
  }
}
