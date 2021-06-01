package de.digitalcollections.iiif.hymir.frontend;

/**
 * Response object to communicate the reason an API error occoured.
 */
public class ApiError {

  private final String reason;

  /**
   * Takes the message from an Exception an sets it as the reason for the API failure.
   * @param exception The exception to take as reason.
   */
  public ApiError(Exception exception) {
    this.reason = exception.getMessage();
  }

  /**
   *
   * @return The reason why the request failed.
   */
  public String getReason() {
    return reason;
  }

}
