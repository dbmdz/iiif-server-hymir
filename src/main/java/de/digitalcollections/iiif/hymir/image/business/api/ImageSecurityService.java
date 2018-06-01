package de.digitalcollections.iiif.hymir.image.business.api;

import java.net.URI;

public interface ImageSecurityService {

  boolean isAccessAllowed(String identifier);

  URI getLicense(String identifier);
}
