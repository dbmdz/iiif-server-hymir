package de.digitalcollections.iiif.hymir.image.backend.impl.repository.v2;

import de.digitalcollections.iiif.hymir.image.backend.impl.repository.jpegtran.v2.JpegTranImage;
import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.hymir.image.model.api.v2.Image;
import de.digitalcollections.iiif.hymir.image.model.api.v2.RegionParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.ResizeParameters;
import de.digitalcollections.iiif.hymir.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.api.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.model.api.exception.UnsupportedFormatException;
import java.awt.Dimension;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Repository;

@Repository(value = "ImageRepositoryJpegTranImpl-v2.0.0")
public class ImageRepositoryJpegTranImpl extends AbstractImageRepositoryImpl implements PriorityOrdered {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageRepositoryJpegTranImpl.class);

  @Override
  @Cacheable(value = "sourceImages")
  protected Image createImage(String identifier, RegionParameters regionParameters) throws InvalidParametersException, ResolvingException, UnsupportedFormatException, IOException {
    LOGGER.debug("Loading image {} with TurboJPEG imlementation.", identifier);
    byte[] imageData = imageDataRepository.getImageData(identifier);
    if ((imageData[0] & 0xFF) != 0xFF || (imageData[1] & 0xFF) != 0xD8) {
      throw new UnsupportedFormatException("Not a JPEG file");
    }
    return new JpegTranImage(imageData);
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  @Override
  public boolean supportsInputFormat(ImageFormat inFormat) {
    return inFormat.equals(ImageFormat.JPEG);
  }

  @Override
  public boolean supportsOutputFormat(ImageFormat outFormat) {
    return outFormat.equals(ImageFormat.JPEG);
  }

  @Override
  public boolean supportsCropOperation(RegionParameters region) {
    return (region == null
            || (region.getWidth() % 8 == 0
            && region.getHeight() % 8 == 0
            && region.getHorizontalOffset() % 8 == 0
            && region.getVerticalOffset() % 8 == 0));
  }

  @Override
  public boolean supportsScaleOperation(Dimension imageDims, ResizeParameters scaleParams) {
    return (scaleParams == null
            || (scaleParams.getWidth() < imageDims.getHeight()
            && scaleParams.getHeight() < imageDims.getWidth()));
  }

  @Override
  public boolean supportsBitDepth(ImageBitDepth bitDepth) {
    return (bitDepth == null
            || bitDepth.equals(ImageBitDepth.GRAYSCALE)
            || bitDepth.equals(ImageBitDepth.COLOR));
  }
}
