package de.digitalcollections.iiif.hymir.presentation.business.api;

import javax.servlet.http.HttpServletRequest;

/** Service responsible for deciding if object (identified by identifier) is accessible. */
public interface PresentationSecurityService {
  @Deprecated(forRemoval = true)  // Will be gone with the next major version

  boolean isAccessAllowed(String identifier);

  default boolean isAccessAllowed(String identifier, HttpServletRequest req) {
    return this.isAccessAllowed(identifier);
  }
}
