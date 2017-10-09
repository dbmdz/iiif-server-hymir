package de.digitalcollections.iiif.hymir.image.business.api.service;

public interface ImageSecurityService {

  boolean isAccessAllowed(String identifier);

}
