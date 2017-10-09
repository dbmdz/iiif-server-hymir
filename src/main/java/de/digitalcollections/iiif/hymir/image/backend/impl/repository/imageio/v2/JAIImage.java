package de.digitalcollections.iiif.hymir.image.backend.impl.repository.imageio.v2;

import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.hymir.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.api.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.hymir.image.model.api.v2.Image;
import de.digitalcollections.iiif.hymir.image.model.api.v2.RegionParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.ResizeParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.TransformationException;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.digitalcollections.iiif.hymir.image.model.api.enums.ImageBitDepth.BITONAL;
import static de.digitalcollections.iiif.hymir.image.model.api.enums.ImageBitDepth.COLOR;
import static de.digitalcollections.iiif.hymir.image.model.api.enums.ImageBitDepth.GRAYSCALE;

public class JAIImage implements Image {

  private static final Logger LOGGER = LoggerFactory.getLogger(JAIImage.class);

  private BufferedImage image;
  private String formatString;

  public JAIImage(BufferedImage image, String format) {
    this.image = image;
    this.formatString = format;
  }

  public JAIImage(byte[] inData) throws IOException, UnsupportedFormatException {
    this(new ByteArrayInputStream(inData), null);
  }

  public JAIImage(byte[] inData, RegionParameters region) throws IOException, UnsupportedFormatException {
    this(new ByteArrayInputStream(inData), region);
  }

  public JAIImage(InputStream imgData, RegionParameters region) throws IOException, UnsupportedFormatException {
    ImageIO.setUseCache(true);
    ImageReader reader = null;
    try (final ImageInputStream imageInputStream = ImageIO.createImageInputStream(imgData)) {
      Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
      if (readers.hasNext()) {
        reader = readers.next();
        this.formatString = reader.getFormatName();
      } else {
        throw new UnsupportedFormatException("Could not read image, unsupported format?");
      }
      reader.setInput(imageInputStream, true, true);
      ImageReadParam params = reader.getDefaultReadParam();
      if (region != null && region.isAbsolute()) {
        int x = (int) Math.ceil(region.getHorizontalOffset());
        int y = (int) Math.ceil(region.getVerticalOffset());
        int width = (int) Math.ceil(region.getWidth());
        int height = (int) Math.ceil(region.getHeight());
        Rectangle rect = new Rectangle(x, y, width, height);
        params.setSourceRegion(rect);
      }
      BufferedImage img = reader.read(0, params);
      this.image = img;
    } finally {
      // Close stream in finally block to avoid resource leaks
      if (reader != null) {
        reader.dispose();
      }
    }
  }

  public static ImageFormat getFormatFromString(String formatName) throws UnsupportedFormatException {
    ImageFormat imageFormat = ImageFormat.getByExtension(formatName.toLowerCase());
    if (imageFormat == null) {
      throw new UnsupportedFormatException();
    }
    return imageFormat;
  }

  @Override
  public Image flipHorizontally() {
    BufferedImage flipped = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
    AffineTransform tran = AffineTransform.getTranslateInstance(image.getWidth(), 0);
    AffineTransform flip = AffineTransform.getScaleInstance(-1d, 1d);
    tran.concatenate(flip);

    Graphics2D g = flipped.createGraphics();
    g.setTransform(tran);
    g.drawImage(image, 0, 0, null);
    g.dispose();

    this.image = flipped;
    return this;
  }

  @Override
  public ImageFormat getFormat() {
    try {
      return getFormatFromString(this.formatString);
    } catch (UnsupportedFormatException ignored) {
      // NOTE: This should never happen
      return null;
    }
  }

  public BufferedImage getImage() {
    return image;
  }

  @Override
  public int getWidth() {
    return image.getWidth();
  }

  @Override
  public int getHeight() {
    return image.getHeight();
  }

  @Override
  public byte[] toByteArray() throws IOException {
    byte[] output;
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      ImageIO.write(this.image, formatString, os);
      output = os.toByteArray();
    }
    return output;
  }

  @Override
  public Image crop(RegionParameters params) throws UnsupportedOperationException, InvalidParametersException {
    int x;
    int y;
    int targetWidth;
    int targetHeight;
    if (params.isAbsolute()) {
      x = (int) Math.ceil(params.getHorizontalOffset());
      y = (int) Math.ceil(params.getVerticalOffset());
      targetWidth = (int) Math.ceil(params.getWidth());
      targetHeight = (int) Math.ceil(params.getHeight());
    } else {
      x = (int) Math.ceil(image.getWidth() * (params.getHorizontalOffset()) / 100);
      y = (int) Math.ceil(image.getHeight() * (params.getVerticalOffset()) / 100);
      targetWidth = (int) Math.ceil(image.getWidth() * (params.getWidth()) / 100);
      targetHeight = (int) Math.ceil(image.getHeight() * (params.getHeight()) / 100);
    }

    if (x >= getWidth() || y >= getHeight()) {
      throw new InvalidParametersException("x and/or y are out of bounds.");
    }
    if ((x + targetWidth) > getWidth()) {
      targetWidth = getWidth() - x;
    }
    if ((y + targetHeight) > getHeight()) {
      targetHeight = getHeight() - y;
    }
    if (targetHeight != this.image.getHeight() || targetWidth != this.image.getWidth()) {
      BufferedImage dest = image.getSubimage(x, y, targetWidth, targetHeight);
      this.image = dest;
    }
    return this;
  }

  @Override
  public Image scale(ResizeParameters params) throws UnsupportedOperationException, InvalidParametersException {
    int oldWidth = getWidth();
    int oldHeight = getHeight();

    int newWidth = (int) Math.ceil(params.getWidth());
    int newHeight = (int) Math.ceil(params.getHeight());

    if (oldWidth == newWidth && oldHeight == newHeight) {
      return this;
    }
    // TODO: make quality scalr method configurable
    this.image = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, newWidth, newHeight);
    return this;
  }

  @Override
  public Image rotate(int arcDegree) throws UnsupportedOperationException, InvalidParametersException {
    if (arcDegree % 90 > 0) {
      throw new UnsupportedOperationException("Can only rotate by multiples of 90 degrees.");
    }
    Scalr.Rotation rotation;
    switch (arcDegree) {
      case -90:
      case 270:
        rotation = Scalr.Rotation.CW_270;
        break;
      case 90:
        rotation = Scalr.Rotation.CW_90;
        break;
      case 180:
        rotation = Scalr.Rotation.CW_180;
        break;
      default:
        return this;
    }
    this.image = Scalr.rotate(image, rotation);
    return this;
  }

  @Override
  public Image toDepth(ImageBitDepth depth) throws UnsupportedOperationException {
    int newColorType = image.getType();
    if (null != depth) {
      switch (depth) {
        case GRAYSCALE:
          newColorType = BufferedImage.TYPE_BYTE_GRAY;
          break;
        case BITONAL:
          newColorType = BufferedImage.TYPE_BYTE_BINARY;
          break;
        case COLOR:
          newColorType = BufferedImage.TYPE_3BYTE_BGR;
          break;
        default:
          break;
      }
    }
    BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), newColorType);
    Graphics2D g2d = newImage.createGraphics();
    g2d.drawImage(image, 0, 0, null);
    image = newImage;
    return this;
  }

  @Override
  public Image convert(ImageFormat targetFormat) throws UnsupportedOperationException, TransformationException {
    // FIXME: png to jpeg conversion results in inverted image...
    ImageFormat sourceFormat = getFormat();
    if (sourceFormat != targetFormat) {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      try {
        ImageIO.write(image, targetFormat.name().toLowerCase(), os);
        InputStream fis = new ByteArrayInputStream(os.toByteArray());
        image = ImageIO.read(fis);
        this.formatString = targetFormat.name();
      } catch (IOException ex) {
        LOGGER.error("Could not read converted image", ex);
        throw new TransformationException("Could not convert image", ex);
      }
    }
    return this;
  }

  @Override
  public void performTransformation() {
    // This class is non-lazy, so this is a NOP
  }
}
