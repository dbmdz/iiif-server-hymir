package de.digitalcollections.iiif.hymir.image.business;

import com.google.common.collect.Streams;
import de.digitalcollections.commons.file.business.impl.resolved.ResolvedFileResourceServiceImpl;
import de.digitalcollections.iiif.hymir.image.business.api.ImageSecurityService;
import de.digitalcollections.iiif.hymir.image.business.api.ImageService;
import de.digitalcollections.iiif.hymir.model.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.model.image.ImageApiProfile;
import de.digitalcollections.iiif.model.image.ImageApiSelector;
import de.digitalcollections.iiif.model.image.ResolvingException;
import de.digitalcollections.iiif.model.image.Size;
import de.digitalcollections.iiif.model.image.TileInfo;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceNotFoundException;
import de.digitalcollections.turbojpeg.imageio.TurboJpegImageReadParam;
import de.digitalcollections.turbojpeg.imageio.TurboJpegImageReader;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ImageServiceImpl implements ImageService {

  private final ImageSecurityService imageSecurityService;
  private final ResolvedFileResourceServiceImpl fileResourceService;

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

  private static class DecodedImage {

    /** Decoded image **/
    final BufferedImage img;

    /** Final target size for scaling **/
    final Dimension targetSize;

    /** Rotation needed after decoding? **/
    final int rotation;

    // Small value type to hold information about decoding results
    protected DecodedImage(BufferedImage img, Dimension targetSize, int rotation) {
      this.img = img;
      this.targetSize = targetSize;
      this.rotation = rotation;
    }
  }

  public ImageServiceImpl(@Autowired(required = false) ImageSecurityService imageSecurityService,
          @Autowired ResolvedFileResourceServiceImpl fileResourceService) {
    this.imageSecurityService = imageSecurityService;
    this.fileResourceService = fileResourceService;
  }

  /** Update ImageService based on the image **/
  private void enrichInfo(ImageReader reader, de.digitalcollections.iiif.model.image.ImageService info) throws IOException {
    ImageApiProfile profile = new ImageApiProfile();
    profile.addFeature(
            ImageApiProfile.Feature.PROFILE_LINK_HEADER,
            ImageApiProfile.Feature.CANONICAL_LINK_HEADER,
            ImageApiProfile.Feature.REGION_SQUARE,
            ImageApiProfile.Feature.ROTATION_BY_90S,
            ImageApiProfile.Feature.MIRRORING,
            ImageApiProfile.Feature.SIZE_ABOVE_FULL);
    profile.addFormat(ImageApiProfile.Format.GIF);

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
      // Cropping aligned to MCUs is faster, and MCUs are either 4, 8 or 16 pixels, so if we stick to multiples
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

  /**
   * Try to obtain a {@link ImageReader} for a given identifier
   */
  private ImageReader getReader(String identifier) throws ResourceNotFoundException, UnsupportedFormatException, IOException {
    if (imageSecurityService != null && !imageSecurityService.isAccessAllowed(identifier)) {
      throw new ResourceNotFoundException();
    }
    FileResource fileResource;
    try {
      fileResource = fileResourceService.find(identifier, MimeType.MIME_IMAGE);
    } catch (ResourceIOException e) {
      throw new ResourceNotFoundException();
    }
    try {
      ImageInputStream iis = ImageIO.createImageInputStream(fileResourceService.getInputStream(fileResource));
      ImageReader reader = Streams.stream(ImageIO.getImageReaders(iis))
          .findFirst()
          .orElseThrow(UnsupportedFormatException::new);
      reader.setInput(iis);
      return reader;
    } catch (ResourceIOException e) {
      throw new ResourceNotFoundException();
    }
  }

  @Override
  public void readImageInfo(String identifier, de.digitalcollections.iiif.model.image.ImageService info)
          throws UnsupportedFormatException, UnsupportedOperationException, ResourceNotFoundException, IOException {
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

  /** Determine parameters for image reading based on the IIIF selector and a given scaling factor **/
  private ImageReadParam getReadParam(ImageReader reader, ImageApiSelector selector, double decodeScaleFactor) throws IOException, InvalidParametersException {
    ImageReadParam readParam = reader.getDefaultReadParam();
    Dimension nativeDimensions = new Dimension(reader.getWidth(0), reader.getHeight(0));
    Rectangle targetRegion;
    try {
      targetRegion = selector.getRegion().resolve(nativeDimensions);
    } catch (ResolvingException e) {
      throw new InvalidParametersException(e);
    }
    // IIIF regions are always relative to the native size, while ImageIO regions are always relative to the decoded
    // image size, hence the conversion
    Rectangle decodeRegion = new Rectangle(
            (int) Math.ceil(targetRegion.getX() * decodeScaleFactor),
            (int) Math.ceil(targetRegion.getY() * decodeScaleFactor),
            (int) Math.ceil(targetRegion.getWidth() * decodeScaleFactor),
            (int) Math.ceil(targetRegion.getHeight() * decodeScaleFactor));
    readParam.setSourceRegion(decodeRegion);
    // TurboJpegImageReader can rotate during decoding
    if (selector.getRotation().getRotation() != 0 && reader instanceof TurboJpegImageReader) {
      ((TurboJpegImageReadParam) readParam).setRotationDegree((int) selector.getRotation().getRotation());
    }
    return readParam;
  }

  /** Decode an image **/
  private DecodedImage readImage(String identifier, ImageApiSelector selector, ImageApiProfile profile) throws IOException, ResourceNotFoundException, UnsupportedFormatException, InvalidParametersException {
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
        if (Math.abs(targetScaleFactor - factor) < Math.abs(targetScaleFactor - decodeScaleFactor)) {
          decodeScaleFactor = factor;
          imageIndex = idx;
        }
      }
      ImageReadParam readParam = getReadParam(reader, selector, decodeScaleFactor);
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
      return new DecodedImage(reader.read(imageIndex, readParam), targetSize, rotation);
    } finally {
      if (reader != null) {
        reader.dispose();
      }
    }
  }

  /** Apply transformations to an decoded image **/
  private BufferedImage transformImage(BufferedImage inputImage, Dimension targetSize, int rotation, boolean mirror,
          ImageApiProfile.Quality quality) {
    BufferedImage img = inputImage;
    final int inType = img.getType();
    boolean needsAdditionalScaling = !new Dimension(img.getWidth(), img.getHeight()).equals(targetSize);
    if (needsAdditionalScaling) {
      img = Scalr.resize(img, Scalr.Method.BALANCED, Scalr.Mode.FIT_EXACT, targetSize.width, targetSize.height);
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
    if (mirror) {
      img = Scalr.rotate(img, Scalr.Rotation.FLIP_HORZ);
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
  public void processImage(String identifier, ImageApiSelector selector, ImageApiProfile profile, OutputStream os)
          throws InvalidParametersException, UnsupportedOperationException, UnsupportedFormatException, ResourceNotFoundException, IOException {
    DecodedImage img = readImage(identifier, selector, profile);
    BufferedImage outImg = transformImage(img.img, img.targetSize, img.rotation, selector.getRotation().isMirror(), selector.getQuality());

    ImageWriter writer = Streams.stream(ImageIO.getImageWriters(new ImageTypeSpecifier(outImg), selector.getFormat().name()))
            .findFirst()
            .orElseThrow(UnsupportedFormatException::new);
    ImageOutputStream ios = ImageIO.createImageOutputStream(os);
    writer.setOutput(ios);
    writer.write(null, new IIOImage(outImg, null, null), null);
    writer.dispose();
    ios.flush();
  }

  @Override
  public Instant getImageModificationDate(String identifier) throws ResourceNotFoundException {
    if (imageSecurityService != null && !imageSecurityService.isAccessAllowed(identifier)) {
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
}
