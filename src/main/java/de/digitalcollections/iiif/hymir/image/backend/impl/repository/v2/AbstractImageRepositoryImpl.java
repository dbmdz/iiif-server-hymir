package de.digitalcollections.iiif.hymir.image.backend.impl.repository.v2;

import de.digitalcollections.iiif.hymir.image.backend.api.repository.v2.ImageRepository;
import de.digitalcollections.iiif.hymir.image.model.api.v2.Image;
import de.digitalcollections.iiif.hymir.image.model.api.v2.ImageInfo;
import de.digitalcollections.iiif.hymir.image.model.api.v2.RegionParameters;
import de.digitalcollections.iiif.hymir.image.model.impl.v2.ImageInfoImpl;
import de.digitalcollections.iiif.hymir.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.api.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.model.api.exception.UnsupportedFormatException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.cache.annotation.CacheResult;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractImageRepositoryImpl implements ImageRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImageRepositoryImpl.class);

  @Autowired
  protected ImageDataRepositoryImpl imageDataRepository;

  protected abstract Image createImage(String identifier, RegionParameters region) throws InvalidParametersException, ResolvingException, UnsupportedFormatException, IOException;

  @Override
  public Image getImage(String identifier, RegionParameters regionParameters) throws InvalidParametersException, UnsupportedOperationException, UnsupportedFormatException {
    Image image;
    try {
      image = createImage(identifier, regionParameters);
    } catch (ResolvingException re) {
      LOGGER.warn("Could not find resolver for {}", identifier, re);
      return null;
    } catch (IOException ioe) {
      LOGGER.error("Could not read image for {}", identifier, ioe);
      return null;
    } catch (IllegalArgumentException arge) {
      throw new InvalidParametersException("Illegal offsets.");
    }
    return image;
  }

  @Override
  @CacheResult(cacheName = "imageInfos")
  public ImageInfo getImageInfo(String identifier) throws UnsupportedFormatException, UnsupportedOperationException {
    ImageInfo imageInfo;
    try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(imageDataRepository.getImageData(identifier)))) {
      final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
      if (readers.hasNext()) {
        ImageReader reader = readers.next();
        reader.setInput(in);
        imageInfo = new ImageInfoImpl();
        final String formatName = reader.getFormatName();
        imageInfo.setHeight(reader.getHeight(0));
        imageInfo.setWidth(reader.getWidth(0));
        reader.dispose();
        return imageInfo;
      } else {
        throw new UnsupportedFormatException("Could not find a reader for " + identifier);
      }
    } catch (Exception e) {
      throw new RuntimeException("Could not get image info for image with identifier " + identifier, e);
    }
  }
}
