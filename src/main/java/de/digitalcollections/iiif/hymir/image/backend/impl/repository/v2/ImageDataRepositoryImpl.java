package de.digitalcollections.iiif.hymir.image.backend.impl.repository.v2;

import de.digitalcollections.core.business.api.ResourceService;
import de.digitalcollections.core.model.api.MimeType;
import de.digitalcollections.core.model.api.resource.Resource;
import de.digitalcollections.core.model.api.resource.enums.ResourcePersistenceType;
import de.digitalcollections.core.model.api.resource.exceptions.ResourceIOException;
import de.digitalcollections.iiif.hymir.model.api.exception.ResolvingException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.cache.annotation.CacheResult;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * Separate class from AbstractImageRepositoryImpl needed, because otherwise methods with caching do not cache if called
 * inside AbstractImageRepositoryImpl
 */
@Repository
public class ImageDataRepositoryImpl {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageDataRepositoryImpl.class);

  @Value("${iiif.image.forceJpeg:false}")
  private boolean forceJpeg;

  @Autowired
  private ResourceService resourceService;

  private byte[] convertToJpeg(byte[] data) throws IOException {
    if ((data[0] & 0xFF) != 0xFF || (data[1] & 0xFF) != 0xD8) {
      BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ImageIO.write(img, "JPEG", os);
      return os.toByteArray();
    } else {
      return data;
    }
  }

  @CacheResult(cacheName = "sourceImages")
  public byte[] getImageData(String identifier) throws ResolvingException {
    Resource resource = getImageResource(identifier);
    URI imageUri = resource.getUri();
    LOGGER.debug("URI for {} is {}", identifier, imageUri.toString());
    return getImageData(imageUri);
  }

  InputStream getImageStream(String identifier) throws ResolvingException, ResourceIOException {
    Resource resource = getImageResource(identifier);
    URI imageUri = resource.getUri();
    LOGGER.debug("URI for {} is {}", identifier, imageUri.toString());
    return resourceService.getInputStream(resource);
  }

  public Resource getImageResource(String identifier) throws ResolvingException {
    Resource resource;
    try {
      resource = resourceService.get(identifier, ResourcePersistenceType.REFERENCED, MimeType.MIME_IMAGE);
    } catch (ResourceIOException ex) {
      LOGGER.warn("Error getting image for identifier " + identifier, ex);
      throw new ResolvingException("No image for identifier " + identifier);
    }
    return resource;
  }

  private byte[] getImageData(URI imageUri) throws ResolvingException {
    String location = imageUri.toString();
    LOGGER.debug("Trying to get image data from: " + location);

    try {
      byte[] imageData;
      InputStream inputStream = resourceService.getInputStream(imageUri);
      imageData = IOUtils.toByteArray(inputStream);

      if (imageData == null || imageData.length == 0) {
        throw new ResolvingException("No image data at location " + location);
      }

      if (forceJpeg) {
        try {
          imageData = convertToJpeg(imageData);
        } catch (IOException e) {
          LOGGER.error("JPEG conversion failed", e);
          throw new ResolvingException("Error converting image from location " + location + "to JPEG.");
        }
      }

      return imageData;
    } catch (IOException ex) {
      LOGGER.warn("Error getting image data from location " + location);
      throw new ResolvingException("No image data for location " + location);
    }
  }
}
