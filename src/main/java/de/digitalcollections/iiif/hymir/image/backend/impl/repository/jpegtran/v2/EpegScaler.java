package de.digitalcollections.iiif.hymir.image.backend.impl.repository.jpegtran.v2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.lang3.time.StopWatch;

public class EpegScaler {

  static {
    try {
      LibraryLoader.loadLibrary("turbojpeg-jni");
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  public static native byte[] downScaleJpegImage(byte[] imageData, int width, int height, int quality);

  public static void main(String[] args) throws IOException {
    int numIterations = 10000;
    Path imgPath = Paths.get(args[0]);
    if (args.length > 1) {
      numIterations = Integer.parseInt(args[1]);
    }
    JpegImage image = new JpegImage(Files.readAllBytes(imgPath));
    JpegImage beaterImage;
    StopWatch sw = new StopWatch();
    for (int i = 0; i < numIterations; i++) {
      sw.reset();
      sw.start();
      beaterImage = new JpegImage(image.toByteArray());
      beaterImage.downScale(250, 250).transform();
      sw.stop();
      System.out.println("Scaling to 250x250 took " + sw.toString());
    }
  }
}
