package de.digitalcollections.iiif.hymir.image.business;

import com.google.common.collect.Streams;
import de.digitalcollections.commons.file.backend.FileSystemResourceIOException;
import de.digitalcollections.commons.file.business.api.FileResourceService;
import de.digitalcollections.iiif.hymir.image.business.api.ImageSecurityService;
import de.digitalcollections.iiif.hymir.image.business.api.ImageService;
import de.digitalcollections.iiif.hymir.model.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.exception.ScalingException;
import de.digitalcollections.iiif.hymir.model.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.model.image.ImageApiProfile;
import de.digitalcollections.iiif.model.image.ImageApiProfile.Format;
import de.digitalcollections.iiif.model.image.ImageApiSelector;
import de.digitalcollections.iiif.model.image.ResolvingException;
import de.digitalcollections.iiif.model.image.Size;
import de.digitalcollections.iiif.model.image.SizeRequest;
import de.digitalcollections.iiif.model.image.TileInfo;
import de.digitalcollections.model.exception.ResourceIOException;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.turbojpeg.imageio.TurboJpegImageReadParam;
import de.digitalcollections.turbojpeg.imageio.TurboJpegImageReader;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ImageServiceImpl implements ImageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageServiceImpl.class);

  // FIXME: Yes, this is incredibly nasty and violates "separation of concerns", but it's the
  //        only way to implement ACL based on user-supplied data without refactoring a significant
  //        part of the API and breaking implementers left and right.
  //        This should be done properly with the next major release that introduces API breakage
  //        anyway
  @Autowired private HttpServletRequest currentRequest;

  /**
   * @param image buffered image to check for alpha channel
   * @return true, if image contains alpha channel
   * @see <a
   *     href="https://docs.oracle.com/javase/8/docs/api/java/awt/image/ColorModel.html#hasAlpha--">Javadoc
   *     ColorModel</a>
   */
  public static boolean containsAlphaChannel(BufferedImage image) {
    return image.getColorModel().hasAlpha();
  }

  private final ImageSecurityService imageSecurityService;
  private final FileResourceService fileResourceService;

  @Value("${custom.iiif.logo:}")
  private String logoUrl;

  @Value("${custom.iiif.attribution:}")
  private String attribution;

  @Value("${custom.iiif.license:}")
  private String license;

  @Value("${custom.iiif.image.maxWidth:65500}")
  private int maxWidth;

  @Value("${custom.iiif.image.maxHeight:65500}")
  private int maxHeight;

  public ImageServiceImpl(
      @Autowired(required = false) ImageSecurityService imageSecurityService,
      @Autowired FileResourceService fileResourceService) {
    this.imageSecurityService = imageSecurityService;
    this.fileResourceService = fileResourceService;
  }

  /** Update ImageService based on the image * */
  private void enrichInfo(
      ImageReader reader, de.digitalcollections.iiif.model.image.ImageService info)
      throws IOException {
    ImageApiProfile profile = new ImageApiProfile();
    profile.addFeature(
        ImageApiProfile.Feature.PROFILE_LINK_HEADER,
        ImageApiProfile.Feature.CANONICAL_LINK_HEADER,
        ImageApiProfile.Feature.REGION_SQUARE,
        ImageApiProfile.Feature.ROTATION_BY_90S,
        ImageApiProfile.Feature.MIRRORING,
        ImageApiProfile.Feature.SIZE_ABOVE_FULL);

    // Add supported Formats
    profile.addFormat(Format.GIF, Format.JPG, Format.PNG, Format.TIF, Format.WEBP);

    // Indicate to the client if we cannot deliver full resolution versions of the image
    if (reader.getHeight(0) > maxHeight || reader.getWidth(0) > maxWidth) {
      profile.setMaxWidth(maxWidth);
      if (maxHeight != maxWidth) {
        profile.setMaxHeight(maxHeight);
      }
    }
    info.addProfile(ImageApiProfile.LEVEL_TWO, profile);

    info.setWidth(reader.getWidth(0));
    info.setHeight(reader.getHeight(0));

    // Check if multiple resolutions are supported
    int numImages = reader.getNumImages(true);
    if (numImages > 1) {
      for (int i = 0; i < numImages; i++) {
        int width = reader.getWidth(i);
        int height = reader.getHeight(i);
        if (width > 1 && height > 1 && width <= maxWidth && height <= maxHeight) {
          info.addSize(new Size(reader.getWidth(i), reader.getHeight(i)));
        }
      }
    }

    // Check if tiling is supported
    if (reader.isImageTiled(0)) {
      int width = reader.getTileWidth(0);
      TileInfo tileInfo = new TileInfo(width);
      for (int i = 0; i < numImages; i++) {
        int scaledWidth = reader.getTileWidth(i);
        tileInfo.addScaleFactor(width / scaledWidth);
      }
      info.addTile(tileInfo);
    } else if (reader instanceof TurboJpegImageReader) {
      // Cropping aligned to MCUs is faster, and MCUs are either 4, 8 or 16 pixels, so if we stick
      // to multiples
      // of 16 for width/height, we are safe.
      if (reader.getWidth(0) >= 512 && reader.getHeight(0) >= 512) {
        TileInfo ti = new TileInfo(512);
        // Scale factors for JPEGs are not always integral, so we hardcode them
        ti.addScaleFactor(1, 2, 4, 8, 16);
        info.addTile(ti);
      }
      if (reader.getWidth(0) >= 1024 && reader.getHeight(0) >= 1024) {
        TileInfo ti = new TileInfo(1024);
        ti.addScaleFactor(1, 2, 4, 8, 16);
        info.addTile(ti);
      }
    }
  }

  /** Try to obtain a {@link ImageReader} for a given identifier */
  private ImageReader getReader(String identifier)
      throws ResourceNotFoundException, UnsupportedFormatException, IOException {
    if (imageSecurityService != null
        && !imageSecurityService.isAccessAllowed(identifier, currentRequest)) {
      throw new ResourceNotFoundException();
    }
    try {
      FileResource fileResource = fileResourceService.find(identifier, MimeType.MIME_IMAGE);
      ImageInputStream iis =
          ImageIO.createImageInputStream(fileResourceService.getInputStream(fileResource));
      ImageReader reader =
          Streams.stream(ImageIO.getImageReaders(iis))
              .findFirst()
              .orElseThrow(UnsupportedFormatException::new);
      reader.setInput(iis);
      return reader;
    } catch (FileSystemResourceIOException e) {
      throw new IOException(e);
    } catch (ResourceIOException e) {
      throw new ResourceNotFoundException();
    }
  }

  @Override
  public void readImageInfo(
      String identifier, de.digitalcollections.iiif.model.image.ImageService info)
      throws UnsupportedFormatException, UnsupportedOperationException, ResourceNotFoundException,
          IOException {
    ImageReader r = null;
    try {
      r = getReader(identifier);
      enrichInfo(r, info);
      if (!this.logoUrl.isEmpty()) {
        info.addLogo(this.logoUrl);
      }
      if (!this.attribution.isEmpty()) {
        info.addAttribution(this.attribution);
      }
      if (!this.license.isEmpty()) {
        info.addLicense(this.license);
      } else if (this.imageSecurityService != null) {
        URI license = this.imageSecurityService.getLicense(identifier);
        if (license != null) {
          info.addLicense(license.toString());
        }
      }
    } finally {
      if (r != null) {
        r.dispose();
      }
    }
  }

  /**
   * Determine parameters for image reading based on the IIIF selector and a given scaling factor *
   */
  static ImageReadParam getReadParam(ImageReader reader, ImageApiSelector selector, int decodeIdx)
      throws IOException, InvalidParametersException {
    ImageReadParam readParam = reader.getDefaultReadParam();
    Dimension nativeDimensions = new Dimension(reader.getWidth(0), reader.getHeight(0));
    Dimension decodeDimensions =
        new Dimension(reader.getWidth(decodeIdx), reader.getHeight(decodeIdx));
    double decodeScaleFactor = decodeDimensions.getWidth() / nativeDimensions.getWidth();
    Rectangle targetRegion;
    try {
      targetRegion = selector.getRegion().resolve(nativeDimensions);
    } catch (ResolvingException e) {
      throw new InvalidParametersException(e);
    }
    // IIIF regions are always relative to the native size, while ImageIO regions are always
    // relative to the decoded
    // image size, hence the conversion
    Rectangle decodeRegion =
        new Rectangle(
            Math.min(
                (int) Math.round(targetRegion.getX() * decodeScaleFactor), decodeDimensions.width),
            Math.min(
                (int) Math.round(targetRegion.getY() * decodeScaleFactor), decodeDimensions.height),
            Math.min(
                (int) Math.round(targetRegion.getWidth() * decodeScaleFactor),
                decodeDimensions.width),
            Math.min(
                (int) Math.round(targetRegion.getHeight() * decodeScaleFactor),
                decodeDimensions.height));
    readParam.setSourceRegion(decodeRegion);
    // TurboJpegImageReader can rotate during decoding
    if (selector.getRotation().getRotation() != 0 && reader instanceof TurboJpegImageReader) {
      ((TurboJpegImageReadParam) readParam)
          .setRotationDegree((int) selector.getRotation().getRotation());
    }
    return readParam;
  }

  /** Decode an image * */
  private DecodedImage readImage(
      String identifier, ImageApiSelector selector, ImageApiProfile profile)
      throws IOException, ResourceNotFoundException, UnsupportedFormatException,
          InvalidParametersException, ScalingException {
    ImageReader reader = null;
    try {
      reader = getReader(identifier);

      if ((selector.getRotation().getRotation() % 90) != 0) {
        throw new UnsupportedOperationException("Can only rotate by multiples of 90 degrees.");
      }

      Dimension nativeDimensions = new Dimension(reader.getWidth(0), reader.getHeight(0));
      Rectangle targetRegion;
      try {
        targetRegion = selector.getRegion().resolve(nativeDimensions);
      } catch (ResolvingException e) {
        throw new InvalidParametersException(e);
      }
      Dimension croppedDimensions = new Dimension(targetRegion.width, targetRegion.height);
      Dimension targetSize;
      try {
        targetSize = selector.getSize().resolve(croppedDimensions, profile);
      } catch (ResolvingException e) {
        throw new InvalidParametersException(e);
      }

      // Determine the closest resolution to the target that can be decoded directly
      double targetScaleFactor = (double) targetSize.width / targetRegion.getWidth();
      double decodeScaleFactor = 1.0;
      int imageIndex = 0;
      for (int idx = 0; idx < reader.getNumImages(true); idx++) {
        double factor = (double) reader.getWidth(idx) / nativeDimensions.width;
        if (factor < targetScaleFactor) {
          continue;
        }
        if (Math.abs(targetScaleFactor - factor)
            < Math.abs(targetScaleFactor - decodeScaleFactor)) {
          decodeScaleFactor = factor;
          imageIndex = idx;
        }
      }
      ImageReadParam readParam = getReadParam(reader, selector, imageIndex);
      int rotation = (int) selector.getRotation().getRotation();
      if (readParam instanceof TurboJpegImageReadParam
          && ((TurboJpegImageReadParam) readParam).getRotationDegree() != 0) {
        if (rotation == 90 || rotation == 270) {
          int w = targetSize.width;
          targetSize.width = targetSize.height;
          targetSize.height = w;
        }
        rotation = 0;
      }

      if (targetSize.width <= 0 || targetSize.height <= 0) {
        throw new ScalingException("Scaling resulted in width or height ≤ 0: " + targetSize);
      }

      return new DecodedImage(reader.read(imageIndex, readParam), targetSize, rotation);
    } finally {
      if (reader != null) {
        reader.dispose();
      }
    }
  }

  /** Apply transformations to an decoded image * */
  private BufferedImage transformImage(
      BufferedImage inputImage,
      Dimension targetSize,
      int rotation,
      boolean mirror,
      ImageApiProfile.Quality quality) {
    BufferedImage img = inputImage;
    final int inType = img.getType();
    boolean needsAdditionalScaling =
        !new Dimension(img.getWidth(), img.getHeight()).equals(targetSize);
    if (needsAdditionalScaling) {
      img =
          Scalr.resize(
              img,
              Scalr.Method.BALANCED,
              Scalr.Mode.FIT_EXACT,
              targetSize.width,
              targetSize.height);
    }

    if (mirror) {
      img = Scalr.rotate(img, Scalr.Rotation.FLIP_HORZ);
    }
    if (rotation != 0) {
      Scalr.Rotation rot;
      switch (rotation) {
        case 90:
          rot = Scalr.Rotation.CW_90;
          break;
        case 180:
          rot = Scalr.Rotation.CW_180;
          break;
        case 270:
          rot = Scalr.Rotation.CW_270;
          break;
        default:
          rot = null;
      }
      img = Scalr.rotate(img, rot);
    }
    // Quality
    int outType;
    switch (quality) {
      case GRAY:
        outType = BufferedImage.TYPE_BYTE_GRAY;
        break;
      case BITONAL:
        outType = BufferedImage.TYPE_BYTE_BINARY;
        break;
      case COLOR:
        outType = BufferedImage.TYPE_3BYTE_BGR;
        break;
      default:
        outType = inType;
    }
    if (outType != img.getType()) {
      BufferedImage newImg = new BufferedImage(img.getWidth(), img.getHeight(), outType);
      Graphics2D g2d = newImg.createGraphics();
      g2d.drawImage(img, 0, 0, null);
      img = newImg;
      g2d.dispose();
    }
    return img;
  }

  @Override
  public void processImage(
      String identifier, ImageApiSelector selector, ImageApiProfile profile, OutputStream os)
      throws InvalidParametersException, UnsupportedOperationException, UnsupportedFormatException,
          ResourceNotFoundException, IOException, ScalingException {

    Rectangle2D region = selector.getRegion().getRegion();
    if (region != null && (region.getWidth() < 1 || region.getHeight() < 1)) {
      String message =
          String.format(
              Locale.ROOT,
              "requested region has to have at least one pixel, but was [x=%.2f, y=%.2f, width=%.2f, height=%.2f]",
              region.getX(),
              region.getY(),
              region.getWidth(),
              region.getHeight());
      throw new InvalidParametersException(message);
    }

    SizeRequest size = selector.getSize();
    if ((size.getWidth() != null && size.getWidth() < 1)
        || (size.getHeight() != null && size.getHeight() < 1)) {
      throw new InvalidParametersException(
          "requested size has to be at least one pixel, but was " + size);
    }

    DecodedImage decodedImage = readImage(identifier, selector, profile);

    boolean containsAlphaChannel = containsAlphaChannel(decodedImage.img);
    LOGGER.debug("image contains alpha channel: " + containsAlphaChannel);
    if (containsAlphaChannel) {
      int type = decodedImage.img.getType();
      LOGGER.debug("image is of type: " + type);
      if (BufferedImage.TYPE_INT_ARGB != type) {
        // make sure to preserve transparency (e.g. of PNGs)
        // see https://github.com/rkalla/imgscalr section "Working with GIFs"
        BufferedImage convertedImage =
            new BufferedImage(
                decodedImage.img.getWidth(),
                decodedImage.img.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        convertedImage.getGraphics().drawImage(decodedImage.img, 0, 0, null);
        convertedImage.getGraphics().dispose();
        decodedImage =
            new DecodedImage(convertedImage, decodedImage.targetSize, decodedImage.rotation);
      }
    }

    BufferedImage outImg =
        transformImage(
            decodedImage.img,
            decodedImage.targetSize,
            decodedImage.rotation,
            selector.getRotation().isMirror(),
            selector.getQuality());

    ImageWriter writer =
        Streams.stream(
                ImageIO.getImageWriters(
                    new ImageTypeSpecifier(outImg), selector.getFormat().name()))
            .findFirst()
            .orElseThrow(UnsupportedFormatException::new);
    ImageOutputStream ios = ImageIO.createImageOutputStream(os);
    writer.setOutput(ios);
    writer.write(outImg);
    writer.dispose();
    ios.flush();
  }

  @Override
  public Instant getImageModificationDate(String identifier) throws ResourceNotFoundException {
    if (imageSecurityService != null
        && !imageSecurityService.isAccessAllowed(identifier, currentRequest)) {
      throw new ResourceNotFoundException();
    }
    try {
      FileResource res = fileResourceService.find(identifier, MimeType.MIME_IMAGE);
      return res.getLastModified().toInstant(ZoneOffset.UTC);
    } catch (ResourceIOException e) {
      throw new ResourceNotFoundException();
    }
  }

  public String getLogoUrl() {
    return logoUrl;
  }

  public void setLogoUrl(String logoUrl) {
    this.logoUrl = logoUrl;
  }

  public String getAttribution() {
    return attribution;
  }

  public void setAttribution(String attribution) {
    this.attribution = attribution;
  }

  public String getLicense() {
    return license;
  }

  public void setLicense(String license) {
    this.license = license;
  }

  public int getMaxWidth() {
    return maxWidth;
  }

  public void setMaxWidth(int maxWidth) {
    this.maxWidth = maxWidth;
  }

  public int getMaxHeight() {
    return maxHeight;
  }

  public void setMaxHeight(int maxHeight) {
    this.maxHeight = maxHeight;
  }

  private static class DecodedImage {

    /** Decoded image * */
    final BufferedImage img;

    /** Final target size for scaling * */
    final Dimension targetSize;

    /** Rotation needed after decoding? * */
    final int rotation;

    // Small value type to hold information about decoding results
    protected DecodedImage(BufferedImage img, Dimension targetSize, int rotation) {
      this.img = img;
      this.targetSize = targetSize;
      this.rotation = rotation;
    }
  }
}
