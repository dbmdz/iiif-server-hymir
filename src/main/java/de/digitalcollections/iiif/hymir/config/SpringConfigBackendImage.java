package de.digitalcollections.iiif.hymir.config;

import de.digitalcollections.commons.file.config.SpringConfigCommonsFile;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Backend configuration. */
@Configuration
@Import(SpringConfigCommonsFile.class)
public class SpringConfigBackendImage {

  private static final Logger log = LoggerFactory.getLogger(SpringConfigBackendImage.class);

  static {
    ImageIO.setUseCache(false); // Use Heap memory for caching instead of disk

    deregisterSunImageSpis();
    String[] readerMimeTypes = ImageIO.getReaderMIMETypes();
    log.debug("ImageIO supported formats (reader): {}", String.join(",", readerMimeTypes));
    for (String mimeType : readerMimeTypes) {
      Iterator<ImageReader> imageReaders = ImageIO.getImageReadersByMIMEType(mimeType);
      while (imageReaders.hasNext()) {
        ImageReader imageReader = imageReaders.next();
        if (imageReader != null) {
          log.debug("ImageReader: {} {}", mimeType, imageReader.getClass().toString());
        }
      }
    }

    String[] writerMimeTypes = ImageIO.getWriterMIMETypes();
    log.debug("ImageIO supported formats (writer): {}", String.join(",", writerMimeTypes));
    for (String writerMimeType : writerMimeTypes) {
      Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByMIMEType(writerMimeType);
      while (imageWriters.hasNext()) {
        ImageWriter imageWriter = imageWriters.next();
        if (imageWriter != null) {
          log.debug("ImageWriter: {} {}", writerMimeType, imageWriter.getClass().toString());
        }
      }
    }
  }

  // TODO: not the proper way in java 11 to deactivate module feature?
  private static void deregisterSunImageSpis() {
    IIORegistry registry = IIORegistry.getDefaultInstance();

    // We need to disable usage of the com.sun.imageio.* classes for TIFF, JPEG and BMP due to
    // strange runtime bugs. Fortunately, the TwelveMonkeys imageio packages for those formats
    // work 🎉
    // We can't block all com.sun.imageio.* SPIs, since GIF and PNG support is not provided by
    // TwelveMonkeys (https://github.com/haraldk/TwelveMonkeys/issues/137).
    Set<String> spis =
        new HashSet<>(
            Arrays.asList(
                "com.sun.imageio.plugins.tiff.TIFFImageReaderSpi",
                "com.sun.imageio.plugins.tiff.TIFFImageWriterSpi",
                "com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi",
                "com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi",
                "com.sun.imageio.plugins.bmp.BMPImageReaderSpi",
                "com.sun.imageio.plugins.bmp.BMPImageWriterSpi"));
    for (String spi : spis) {
      try {
        Object spiProvider = registry.getServiceProviderByClass(Class.forName(spi));
        registry.deregisterServiceProvider(spiProvider);
      } catch (ClassNotFoundException e) {
        log.debug(e.getMessage());
      }
    }
  }
}
