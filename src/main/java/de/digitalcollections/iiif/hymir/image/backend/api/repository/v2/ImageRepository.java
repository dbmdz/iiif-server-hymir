package de.digitalcollections.iiif.hymir.image.backend.api.repository.v2;

import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.hymir.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.api.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.hymir.image.model.api.v2.Image;
import de.digitalcollections.iiif.hymir.image.model.api.v2.ImageInfo;
import de.digitalcollections.iiif.hymir.image.model.api.v2.RegionParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.ResizeParameters;
import java.awt.Dimension;

public interface ImageRepository {

  public ImageInfo getImageInfo(String identifier) throws UnsupportedFormatException, UnsupportedOperationException;

  public Image getImage(String identifier, RegionParameters regionParameters) throws InvalidParametersException, UnsupportedOperationException, UnsupportedFormatException;

  public boolean supportsInputFormat(ImageFormat inFormat);

  public boolean supportsOutputFormat(ImageFormat outFormat);

  public boolean supportsCropOperation(RegionParameters region);

  public boolean supportsScaleOperation(Dimension imageDims, ResizeParameters scaleParams);

  public boolean supportsBitDepth(ImageBitDepth bitDepth);
}
