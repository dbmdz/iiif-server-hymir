package de.digitalcollections.iiif.hymir.image.backend.impl.repository.jpegtran.v2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.TJDecompressor;
import org.libjpegturbo.turbojpeg.TJException;
import org.libjpegturbo.turbojpeg.TJTransform;
import org.libjpegturbo.turbojpeg.TJTransformer;

public class JpegImage {

  // Only used to obtain width, height and subsampling information
  private TJDecompressor decomp;
  private TJTransform transformOptions;
  private byte[] imgData;

  /**
   * Read JPEG image from URI.
   *
   * @param filePath file path to the image
   * @throws IOException if stream cannot be opened on file
   */
  public JpegImage(URI filePath) throws IOException {
    this(IOUtils.toByteArray(filePath));
  }

  /**
   * Read JPEG image from byte array.
   *
   * @param data create image from byte array
   * @throws TJException if error in native code
   */
  public JpegImage(byte[] data) throws TJException {
    this.setImageData(data);
    this.setTransformOptions(new TJTransform());
  }

  /**
   * @param data image data
   * @throws TJException  if error in native code
   */
  public void setImageData(byte[] data) throws TJException {
    if ((data[0] & 0xFF) != 0xFF || (data[1] & 0xFF) != 0xD8) {
      throw new IllegalArgumentException("Not a JPEG file");
    }
    this.imgData = data;
    this.decomp = new TJDecompressor(this.imgData);
  }

  public void setTransformOptions(TJTransform transformOptions) {
    this.transformOptions = transformOptions;
  }

  /**
   * @return width of image in pixels
   */
  public int getWidth() {
    return this.decomp.getWidth();
  }

  /**
   * @return height of image in pixels
   */
  public int getHeight() {
    return this.decomp.getHeight();
  }

  /**
   * Rotate image
   *
   * @param angle Degree to rotate. Must be 90, 180 or 270.
   * @return rotated image
   */
  public JpegImage rotate(int angle) {
    if (angle % 90 != 0 || angle < 0 || angle > 270) {
      throw new IllegalArgumentException("Degree must be 90, 180 or 270");
    }
    switch (angle) {
      case 90:
        this.transformOptions.op = TJTransform.OP_ROT90;
        break;
      case 180:
        this.transformOptions.op = TJTransform.OP_ROT180;
        break;
      case 270:
        this.transformOptions.op = TJTransform.OP_ROT270;
        break;
      default:
        break;
    }
    return this;
  }

  /**
   * Flip the image in horizontal direction.
   * @return horizontally flipped image
   */
  public JpegImage flipHorizontal() {
    this.transformOptions.op = TJTransform.OP_HFLIP;
    return this;
  }

  /**
   * Flip the image in vertical direction.
   * @return vertically flipped image
   */
  public JpegImage flipVertical() {
    this.transformOptions.op = TJTransform.OP_VFLIP;
    return this;
  }

  /**
   * Transpose the image.
   * @return transposed image
   */
  public JpegImage transpose() {
    this.transformOptions.op = TJTransform.OP_TRANSPOSE;
    return this;
  }

  /**
   * Transverse transpose the image.
   * @return transversed image
   */
  public JpegImage transverse() {
    this.transformOptions.op = TJTransform.OP_TRANSVERSE;
    return this;
  }

  /**
   * Downscale the image.
   * @param width Desired width in pixels, must be smaller than original width
   * @param height Desired height in pixels, must be smaller than original height
   * @return scaled image
   * @throws TJException if error in native code
   */
  public JpegImage downScale(int width, int height) throws TJException {
    return downScale(width, height, 75);
  }

  /**
   * @param width Desired width in pixels, must be smaller than original width
   * @param height Desired height in pixels, must be smaller than original height
   * @param quality quality of target image
   * @return scaled image
   * @throws TJException if error in native code
   */
  public JpegImage downScale(int width, int height, int quality) throws TJException {
    // Do we need to apply some transformations beforehand?
    if (this.transformOptions.op != 0 || !this.transformOptions.isEmpty()) {
      this.transform();
      this.setTransformOptions(new TJTransform());
    }

    if (width > getWidth() || height > getHeight()) {
      throw new IllegalArgumentException(String.format(
              "Target dimensions must be smaller than original dimensions, were %sx%s vs %sx%s.",
              getWidth(), getHeight(), width, height));
    }
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("Width and height must be greater than 0");
    }
    this.setImageData(EpegScaler.downScaleJpegImage(this.imgData, width, height, quality));
    return this;
  }

  /**
   * Crop a region out of the image.
   * @param x horizontal offset of region
   * @param y vertical offset of region
   * @param width width of region
   * @param height height of region
   * @return cropped image
   */
  public JpegImage crop(int x, int y, int width, int height) {
    if (width > (this.getWidth() - x) || height > (this.getHeight() - y)) {
      throw new IllegalArgumentException("Width or height exceed the boundaries of the cropped image, check the vertical/horizontal offset!");
    }
    if (x < 0 || y < 0) {
      throw new IllegalArgumentException("Vertical and horizontal offsets cannot be negative.");
    }
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("Width and height must be greater than 0");

    }
    this.transformOptions.setBounds(x, y, width, height);
    this.transformOptions.options |= TJTransform.OPT_CROP;
    return this;
  }

  /**
   * Convert the image to grayscale.
   * @return image converted to grayscale
   */
  public JpegImage toGrayscale() {
    this.transformOptions.options |= TJTransform.OPT_GRAY;
    return this;
  }

  /**
   * @return transformed image
   * @throws TJException if error in native code
   */
  public JpegImage transform() throws TJException {
    TJTransformer transformer = new TJTransformer(imgData);
    int destinationSize = TJ.bufSize(getWidth(), getHeight(), this.decomp.getSubsamp());
    byte[][] destBufs = new byte[1][destinationSize];
    transformer.transform(destBufs, new TJTransform[]{this.transformOptions}, 0);
    int[] transformedSizes = transformer.getTransformedSizes();
    this.setImageData(Arrays.copyOfRange(destBufs[0], 0, transformedSizes[0]));
    this.transformOptions = new TJTransform();
    return this;
  }

  /**
   * @return image as byte array
   */
  public byte[] toByteArray() {
    return imgData;
  }

  /**
   * Write image to given file path.
   *
   * @param filePath target file path
   * @throws IOException if writing to output stream fails
   */
  public void write(URI filePath) throws IOException {
    File outFile = new File(filePath.toURL().getFile());
    if (!outFile.exists()) {
      outFile.createNewFile();
    }
    IOUtils.write(imgData, new FileOutputStream(outFile));
  }
}
