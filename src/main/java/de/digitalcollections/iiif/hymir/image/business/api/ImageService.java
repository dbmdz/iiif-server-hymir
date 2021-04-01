package de.digitalcollections.iiif.hymir.image.business.api;

import de.digitalcollections.iiif.hymir.model.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.model.exception.SecurityException;
import de.digitalcollections.iiif.hymir.model.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.model.image.ImageApiProfile;
import de.digitalcollections.iiif.model.image.ImageApiSelector;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

/** Service providing image processing functionality. */
public interface ImageService {

  default Instant getImageModificationDate(String identifier)
      throws SecurityException, ResolvingException, IOException {
    return null;
  }

  void readImageInfo(String identifier, de.digitalcollections.iiif.model.image.ImageService info)
      throws UnsupportedFormatException, UnsupportedOperationException, IOException, ResolvingException, SecurityException;

  void processImage(
      String identifier, ImageApiSelector selector, ImageApiProfile profile, OutputStream os)
      throws InvalidParametersException, UnsupportedOperationException, UnsupportedFormatException, IOException, ResolvingException, SecurityException;
}
