package de.digitalcollections.iiif.hymir.config;

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

    deregisterSunImageSpis();
    String[] readerMimeTypes = ImageIO.getReaderMIMETypes();
    LOGGER.info("ImageIO supported formats (reader): {}", String.join(",", readerMimeTypes));
    for (String mimeType : readerMimeTypes) {
      Iterator<ImageReader> imageReaders = ImageIO.getImageReadersByMIMEType(mimeType);
      for (Iterator iterator = imageReaders; iterator.hasNext(); ) {
        ImageReader imageReader = (ImageReader) iterator.next();
        if (imageReader != null) {
          LOGGER.info("ImageReader: {} {}", mimeType, imageReader.getClass().toString());
        }
      }
    }

    String[] writerMimeTypes = ImageIO.getWriterMIMETypes();
    LOGGER.info("ImageIO supported formats (writer): {}", String.join(",", writerMimeTypes));
    for (String writerMimeType : writerMimeTypes) {
      Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByMIMEType(writerMimeType);
      for (Iterator iterator = imageWriters; iterator.hasNext(); ) {
        ImageWriter imageWriter = (ImageWriter) iterator.next();
        if (imageWriter != null) {
          LOGGER.info("ImageWriter: {} {}", writerMimeType, imageWriter.getClass().toString());
        }
      }
    }
  }

  // TODO: not the proper way in java 11 to deactivate module feature?
  private static void deregisterSunImageSpis() {
    IIORegistry registry = IIORegistry.getDefaultInstance();
    try {
      // We need to disable using the com.sun.imageio.* classes for tiff and jpeg due to strange runtime bugs.
      // But we can just rely on the TwelveMonkeys imageio packages 🎉
      // gif and png support is not provided by TwelveMonkeys (https://github.com/haraldk/TwelveMonkeys/issues/137)
      // so we do not need to disable PNGImage{Reader,Writer}Spi and GIFImage{Reader, Writer}Spi for the com.sun.imageio.* classes here.
      Set<String> spis = new HashSet<>(Arrays.asList("com.sun.imageio.plugins.tiff.TIFFImageReaderSpi", "com.sun.imageio.plugins.tiff.TIFFImageWriterSpi",
          "com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi", "com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi",
          "com.sun.imageio.plugins.bmp.BMPImageReaderSpi", "com.sun.imageio.plugins.bmp.BMPImageWriterSpi"));
      for (String spi : spis) {
        Object spiProvider = registry.getServiceProviderByClass(Class.forName(spi));
        registry.deregisterServiceProvider(spiProvider);
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
}
