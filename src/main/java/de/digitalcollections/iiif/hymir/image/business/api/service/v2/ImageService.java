package de.digitalcollections.iiif.hymir.image.business.api.service.v2;

import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.hymir.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.api.exception.ResourceNotFoundException;
import de.digitalcollections.iiif.hymir.model.api.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.hymir.image.model.api.v2.Image;
import de.digitalcollections.iiif.hymir.image.model.api.v2.ImageInfo;
import de.digitalcollections.iiif.hymir.image.model.api.v2.RegionParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.ResizeParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.RotationParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.TransformationException;

/**
 * Service providing image processing functionality.
 */
public interface ImageService {

  ImageInfo getImageInfo(String identifier) throws UnsupportedFormatException, UnsupportedOperationException, ResourceNotFoundException;

  Image processImage(String identifier, RegionParameters regionParameters, ResizeParameters sizeParameters, RotationParameters rotationParameters, ImageBitDepth bitDepthParameter, ImageFormat formatParameter)
          throws InvalidParametersException, UnsupportedOperationException, UnsupportedFormatException, TransformationException, ResourceNotFoundException;

}
