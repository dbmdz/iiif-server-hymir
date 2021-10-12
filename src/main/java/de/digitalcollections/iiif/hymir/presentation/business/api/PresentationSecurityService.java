package de.digitalcollections.iiif.hymir.presentation.business.api;

import javax.servlet.http.HttpServletRequest;

/** Service responsible for deciding if object (identified by identifier) is accessible. */
public interface PresentationSecurityService {

  boolean isAccessAllowed(String identifier);

  default boolean isAccessAllowed(String identifier, HttpServletRequest req) {
    return this.isAccessAllowed(identifier);
  }
}
