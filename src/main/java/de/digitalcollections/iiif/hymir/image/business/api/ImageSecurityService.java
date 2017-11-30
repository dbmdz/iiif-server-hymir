package de.digitalcollections.iiif.hymir.image.business.api;

public interface ImageSecurityService {

  boolean isAccessAllowed(String identifier);

}
