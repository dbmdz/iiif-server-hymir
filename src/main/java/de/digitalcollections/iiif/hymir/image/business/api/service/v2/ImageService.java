package de.digitalcollections.iiif.hymir.image.business.api.service.v2;

import de.digitalcollections.core.model.api.resource.exceptions.ResourceIOException;
import de.digitalcollections.iiif.hymir.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.api.exception.ResourceNotFoundException;
import de.digitalcollections.iiif.hymir.model.api.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.model.image.ImageApiSelector;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Service providing image processing functionality.
 */
public interface ImageService {

  void readImageInfo(String identifier, de.digitalcollections.iiif.model.image.ImageService info)
      throws UnsupportedFormatException, UnsupportedOperationException, ResourceNotFoundException, IOException;

  void processImage(String identifier, ImageApiSelector selector, OutputStream os)
      throws InvalidParametersException, UnsupportedOperationException, UnsupportedFormatException,
             ResourceNotFoundException, IOException;

}
