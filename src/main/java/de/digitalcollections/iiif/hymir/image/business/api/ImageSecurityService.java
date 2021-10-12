package de.digitalcollections.iiif.hymir.image.business.api;

import java.net.URI;
import javax.servlet.http.HttpServletRequest;

public interface ImageSecurityService {

  boolean isAccessAllowed(String identifier);

  default boolean isAccessAllowed(String identifier, HttpServletRequest req) {
    return this.isAccessAllowed(identifier);
  }

  URI getLicense(String identifier);
}
