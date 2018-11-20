package de.digitalcollections.iiif.hymir.config;

import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Backend configuration.
 */
@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.commons.file.config"
})
public class SpringConfigBackendImage {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackendImage.class);

  static {
    ImageIO.setUseCache(false);  // Use Heap memory for caching instead of disk

    String[] readerMimeTypes = ImageIO.getReaderMIMETypes();
    LOGGER.info("ImageIO supported formats (reader): {}", String.join(",", readerMimeTypes));
    for (String mimeType : readerMimeTypes) {
      Iterator<ImageReader> imageReaders = ImageIO.getImageReadersByMIMEType(mimeType);
      for (Iterator iterator = imageReaders; iterator.hasNext();) {
        ImageReader imageReader = (ImageReader) iterator.next();
        LOGGER.info("ImageReader: {} {}", mimeType, imageReader.getClass().toString());
      }
    }

    String[] writerMimeTypes = ImageIO.getWriterMIMETypes();
    LOGGER.info("ImageIO supported formats (writer): {}", String.join(",", writerMimeTypes));
    for (String writerMimeType : writerMimeTypes) {
      Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByMIMEType(writerMimeType);
      for (Iterator iterator = imageWriters; iterator.hasNext();) {
        ImageWriter imageWriter = (ImageWriter) iterator.next();
        LOGGER.info("ImageWriter: {} {}", writerMimeType, imageWriter.getClass().toString());
      }
    }
  }
}
