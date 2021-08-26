package de.digitalcollections.iiif.hymir.image.business.api;

import de.digitalcollections.iiif.hymir.model.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.exception.ScalingException;
import de.digitalcollections.iiif.hymir.model.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.model.image.ImageApiProfile;
import de.digitalcollections.iiif.model.image.ImageApiSelector;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

/** Service providing image processing functionality. */
public interface ImageService {

  default Instant getImageModificationDate(String identifier) throws ResourceNotFoundException {
    return null;
  }

  void readImageInfo(String identifier, de.digitalcollections.iiif.model.image.ImageService info)
      throws UnsupportedFormatException, UnsupportedOperationException, ResourceNotFoundException,
          IOException;

  void processImage(
      String identifier, ImageApiSelector selector, ImageApiProfile profile, OutputStream os)
      throws InvalidParametersException, UnsupportedOperationException, UnsupportedFormatException,
          ResourceNotFoundException, IOException, ScalingException;
}
