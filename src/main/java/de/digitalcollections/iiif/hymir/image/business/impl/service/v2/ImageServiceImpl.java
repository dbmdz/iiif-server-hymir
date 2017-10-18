package de.digitalcollections.iiif.hymir.image.business.impl.service.v2;

import de.digitalcollections.iiif.hymir.image.backend.api.repository.v2.ImageRepository;
import de.digitalcollections.iiif.hymir.image.business.api.service.ImageSecurityService;
import de.digitalcollections.iiif.hymir.image.business.api.service.v2.ImageService;
import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.hymir.image.model.api.v2.Image;
import de.digitalcollections.iiif.hymir.image.model.api.v2.ImageInfo;
import de.digitalcollections.iiif.hymir.image.model.api.v2.RegionParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.ResizeParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.RotationParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.TransformationException;
import de.digitalcollections.iiif.hymir.image.model.impl.v2.ResizeParametersImpl;
import de.digitalcollections.iiif.hymir.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.api.exception.ResourceNotFoundException;
import de.digitalcollections.iiif.hymir.model.api.exception.UnsupportedFormatException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "ImageServiceImpl-v2.0.0")
public class ImageServiceImpl implements ImageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageServiceImpl.class);

  @Autowired
  private List<ImageRepository> imageRepositories;

  @Autowired(required = false)
  private ImageSecurityService imageSecurityService;

  @Override
  public ImageInfo getImageInfo(String identifier) throws UnsupportedFormatException, UnsupportedOperationException, ResourceNotFoundException {
    if (imageSecurityService != null && !imageSecurityService.isAccessAllowed(identifier)) {
      throw new ResourceNotFoundException();
    }
    // FIXME: This is really ugly, but unfortunately there's no way to tell from the identifier what
    // format we're dealing with...
    for (ImageRepository repo : this.imageRepositories) {
      try {
        ImageInfo info = repo.getImageInfo(identifier);
        return info;
      } catch (Throwable repoNotWorking) {
      }
    }
    throw new UnsupportedFormatException();
  }

  private Image getImage(String identifier, RegionParameters regionParameters, ImageFormat outputFormat, ImageBitDepth bitDepthParameter)
          throws UnsupportedFormatException, InvalidParametersException, UnsupportedOperationException {
    for (ImageRepository repo : this.imageRepositories) {
      if (!repo.supportsCropOperation(regionParameters)
              || !repo.supportsOutputFormat(outputFormat)
              || !repo.supportsBitDepth(bitDepthParameter)) {
        continue;
      }
      try {
        Image image = repo.getImage(identifier, regionParameters);
        return image;
      } catch (InvalidParametersException e) {
        throw e;
      } catch (UnsupportedOperationException e) {
        // try it with next repository
      } catch (Throwable repoNotWorking) {
        // try it with next repository
      }
    }
    throw new UnsupportedFormatException();
  }

  @Override
  public Image processImage(String identifier, RegionParameters regionParameters, ResizeParameters sizeParameters, RotationParameters rotationParameters, ImageBitDepth bitDepthParameter, ImageFormat formatParameter)
          throws InvalidParametersException, UnsupportedOperationException, UnsupportedFormatException, TransformationException, ResourceNotFoundException {
    if (imageSecurityService != null && !imageSecurityService.isAccessAllowed(identifier)) {
      LOGGER.info("Access to image '{}' is not allowed!", identifier);
      throw new ResourceNotFoundException();
    }
    Image image = getImage(identifier, regionParameters, formatParameter, bitDepthParameter);
    if (image == null) {
      throw new ResourceNotFoundException();
    }
    image = transformImage(image, regionParameters, sizeParameters, rotationParameters, bitDepthParameter, formatParameter);
    return image;
  }

  private Image transformImage(Image image, RegionParameters regionParameters, ResizeParameters sizeParameters,
          RotationParameters rotationParameters, ImageBitDepth bitDepthParameter,
          ImageFormat formatParameter)
          throws InvalidParametersException, UnsupportedOperationException, UnsupportedFormatException, TransformationException {

    // Convert relative to absolute region parameters
    if (regionParameters != null && !regionParameters.isAbsolute()) {
      regionParameters.makeAbsolute(image.getWidth(), image.getHeight());
    }

    // now do processing:
    if (regionParameters != null && (image.getWidth() != regionParameters.getWidth()
            || image.getHeight() != regionParameters.getHeight())) {
      if ("square".equals(regionParameters.getProcessType())) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int x;
        int y;
        int targetWidth;
        int targetHeight;
        if (imageHeight < imageWidth) {
          x = (int) Math.round((image.getWidth() - image.getHeight()) / 2);
          y = 0;
          targetWidth = imageHeight;
          targetHeight = imageHeight;
        } else if (imageHeight > imageWidth) {
          x = 0;
          y = (int) Math.round((imageHeight - imageWidth) / 2);
          targetWidth = imageWidth;
          targetHeight = imageWidth;
        } else {
          x = 0;
          y = 0;
          targetWidth = imageWidth;
          targetHeight = imageHeight;
        }
        regionParameters.setHorizontalOffset(x);
        regionParameters.setVerticalOffset(y);
        regionParameters.setWidth(targetWidth);
        regionParameters.setHeight(targetHeight);
      }
      image = image.crop(regionParameters);
    }

    if (sizeParameters != null) {
      int sourceWidth = regionParameters != null ? (int) regionParameters.getWidth() : image.getWidth();
      int sourceHeight = regionParameters != null ? (int) regionParameters.getHeight() : image.getHeight();
      sizeParameters = new ResizeParametersImpl(sizeParameters, sourceWidth, sourceHeight);
    }

    if (sizeParameters != null && sizeParameters.getMaxHeight() != -1
            && sizeParameters.getMaxWidth() != -1) {
      image = image.scale(sizeParameters);
    }
    if (rotationParameters != null) {
      if (rotationParameters.isMirrorHorizontally()) {
        image = image.flipHorizontally();
      }
      if (rotationParameters.getDegrees() > 0) {
        image = image.rotate(rotationParameters.getDegrees());
      }
    }
    if (bitDepthParameter != null) {
      image = image.toDepth(bitDepthParameter);
    }
    if (!(formatParameter == image.getFormat())) {
      image = image.convert(formatParameter);
    }
    image.performTransformation();
    return image;
  }

}
