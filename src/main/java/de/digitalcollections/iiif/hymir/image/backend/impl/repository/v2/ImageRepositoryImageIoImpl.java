package de.digitalcollections.iiif.hymir.image.backend.impl.repository.v2;

import de.digitalcollections.iiif.hymir.image.backend.impl.repository.imageio.v2.JAIImage;
import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.hymir.model.api.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.model.api.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.hymir.image.model.api.v2.Image;
import de.digitalcollections.iiif.hymir.image.model.api.v2.RegionParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.ResizeParameters;
import java.awt.Dimension;
import java.io.IOException;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import static de.digitalcollections.iiif.hymir.image.model.api.enums.ImageBitDepth.BITONAL;
import static de.digitalcollections.iiif.hymir.image.model.api.enums.ImageBitDepth.COLOR;
import static de.digitalcollections.iiif.hymir.image.model.api.enums.ImageBitDepth.GRAYSCALE;

@Repository(value = "ImageRepositoryImageIoImpl-v2.0.0")
public class ImageRepositoryImageIoImpl extends AbstractImageRepositoryImpl {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageRepositoryImageIoImpl.class);

  @Override
  protected Image createImage(String identifier, RegionParameters regionParameters) throws ResolvingException, UnsupportedFormatException, IOException {
    LOGGER.debug("Loading image {} with ImageIO imlementation.", identifier);
    byte[] imageData = imageDataRepository.getImageData(identifier);
    return new JAIImage(imageData, regionParameters);
  }

  @Override
  public boolean supportsInputFormat(ImageFormat inFormat) {
    return Stream.of(ImageIO.getReaderFormatNames())
            .anyMatch(name -> {
              try {
                return JAIImage.getFormatFromString(name).equals(inFormat);
              } catch (UnsupportedFormatException e) {
                return false;
              }
            });
  }

  @Override
  public boolean supportsOutputFormat(ImageFormat outFormat) {
    return Stream.of(ImageIO.getWriterFormatNames())
            .anyMatch(name -> {
              try {
                return JAIImage.getFormatFromString(name).equals(outFormat);
              } catch (UnsupportedFormatException e) {
                return false;
              }
            });
  }

  @Override
  public boolean supportsCropOperation(RegionParameters region) {
    // No limitations on cropping
    return true;
  }

  @Override
  public boolean supportsScaleOperation(Dimension imageDims, ResizeParameters scaleParams) {
    // No limitations on scaling
    return true;
  }

  @Override
  public boolean supportsBitDepth(ImageBitDepth bitDepth) {
    if (bitDepth == null) {
      return true;
    }
    switch (bitDepth) {
      case BITONAL:
      case GRAYSCALE:
      case COLOR:
        return true;
      default:
        return false;
    }
  }
}
