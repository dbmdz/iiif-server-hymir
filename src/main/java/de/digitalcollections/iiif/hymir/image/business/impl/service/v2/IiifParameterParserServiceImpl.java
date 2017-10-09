package de.digitalcollections.iiif.hymir.image.business.impl.service.v2;

import de.digitalcollections.iiif.hymir.image.business.api.service.v2.IiifParameterParserService;
import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.hymir.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.api.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.hymir.image.model.api.v2.RegionParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.ResizeParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.RotationParameters;
import de.digitalcollections.iiif.hymir.image.model.impl.v2.RegionParametersImpl;
import de.digitalcollections.iiif.hymir.image.model.impl.v2.ResizeParametersImpl;
import de.digitalcollections.iiif.hymir.image.model.impl.v2.RotationParametersImpl;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service(value = "IiifParameterParserServiceImpl-v2.0.0")
public class IiifParameterParserServiceImpl implements IiifParameterParserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IiifParameterParserServiceImpl.class);

  private static DoubleStream makeDoubleStream(float[] vals) {
    return IntStream.range(0, vals.length)
            .mapToDouble(i -> vals[i]);
  }

  private float[] parseFloatValues(String values, int expectedSize) throws InvalidParametersException {
    String[] groups = values.split(",");
    if (groups.length != expectedSize) {
      LOGGER.warn("Invalid values definition. {} float numbers needed!", expectedSize);
      throw new InvalidParametersException("Invalid values definition. " + expectedSize + " float numbers needed!");
    }
    float[] floatValues = new float[expectedSize];
    for (int i = 0; i < groups.length; i++) {
      try {
        floatValues[i] = Float.parseFloat(groups[i]);
      } catch (NumberFormatException nfe) {
        LOGGER.warn("Invalid value number: {}.", groups[i]);
        throw new InvalidParametersException("Invalid value number. Must be integer or float");
      }
    }
    return floatValues;
  }

  @Override
  public ImageFormat parseIiifFormat(String targetFormat) throws UnsupportedFormatException {
    ImageFormat format = ImageFormat.getByExtension(targetFormat);
    if (format == null) {
      throw new UnsupportedFormatException();
    }
    return format;
  }

  @Override
  public ImageBitDepth parseIiifQuality(String targetQuality) throws InvalidParametersException {
    switch (targetQuality) {
      case "bitonal":
        return ImageBitDepth.BITONAL;
      case "color":
        return ImageBitDepth.COLOR;
      case "default":
        return null;
      case "gray":
        return ImageBitDepth.GRAYSCALE;
      case "native":
        return null;
      default:
        throw new InvalidParametersException();
    }
  }

  @Override
  public RegionParameters parseIiifRegion(String region) throws InvalidParametersException {
    assert region != null;
    if ("full".equals(region)) {
      // The complete image is returned, without any cropping.
      return null; // indicates that no region has to be cropped
    }
    float[] dimensions;
    RegionParameters params = new RegionParametersImpl();
    if (region.startsWith("pct:")) {
      region = region.substring("pct:".length());
      dimensions = parseFloatValues(region, 4);
      params.setAbsolute(false);
      for (int i = 0; i < dimensions.length; i++) {
        dimensions[i] = dimensions[i] / 100;
      }
    } else {
      dimensions = parseFloatValues(region, 4);
      params.setAbsolute(true);
    }
    params.setHorizontalOffset(dimensions[0]);
    params.setVerticalOffset(dimensions[1]);
    params.setWidth(dimensions[2]);
    params.setHeight(dimensions[3]);
    return params;
  }

  @Override
  public RotationParameters parseIiifRotation(String rotation) throws InvalidParametersException {
    assert rotation != null;
    RotationParameters params = new RotationParametersImpl();
    if (rotation.startsWith("!")) {
      params.setMirrorHorizontally(true);
      rotation = rotation.substring(1);
    }
    int degrees = parseIntegerValue(rotation);
    if (degrees < 0 || degrees > 360) {
      throw new InvalidParametersException("The degrees of clockwise rotation must be between 0 and 360!");
    }
    params.setDegrees(degrees);
    return params;
  }

  @Override
  public ResizeParameters parseIiifSize(String size) throws InvalidParametersException {
    assert size != null;
    if ("full".equals(size)) {
      // The extracted region is not scaled, and is returned at its full size.
      return null; // indicates no resizing
    }
    ResizeParameters params = new ResizeParametersImpl();
    if (size.startsWith("pct:")) {
      int scaleFactor = parseIntegerValue(size.substring("pct:".length()));
      params.setScaleFactor(scaleFactor);
    } else if (size.endsWith(",")) {
      int targetWidth = parseIntegerValue(size.substring(0, size.lastIndexOf(",")));
      params.setWidth(targetWidth);
    } else if (size.startsWith(",")) {
      int targetHeight = parseIntegerValue(size.substring(1));
      params.setHeight(targetHeight);
    } else if (size.startsWith("!")) {
      int[] values = parseIntegerValues(size.substring(1), 2);
      params.setMaxWidth(values[0]);
      params.setMaxHeight(values[1]);
    } else {
      int[] values = parseIntegerValues(size, 2);
      params.setWidth(values[0]);
      params.setHeight(values[1]);
    }
    return params;
  }

  private int parseIntegerValue(String value) throws InvalidParametersException {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException nfe) {
      LOGGER.warn("Invalid format: {}.", value);
      throw new InvalidParametersException("Invalid format. Must be an integer!");
    }
  }

  private int[] parseIntegerValues(String values, int expectedSize) throws InvalidParametersException {
    String[] groups = values.split(",");
    if (groups.length != expectedSize) {
      LOGGER.warn("Invalid values definition. {} integer numbers needed!", expectedSize);
      throw new InvalidParametersException("Invalid values definition. " + expectedSize + " integer numbers needed!");
    }
    int[] integerValues = new int[expectedSize];
    for (int i = 0; i < groups.length; i++) {
      try {
        integerValues[i] = Integer.parseInt(groups[i]);
      } catch (NumberFormatException nfe) {
        LOGGER.warn("Invalid value number: {}.", groups[i]);
        throw new InvalidParametersException("Invalid value number. Must be an integer.");
      }
    }
    return integerValues;
  }

}
