package de.digitalcollections.iiif.hymir.image.model.impl.v2;

import de.digitalcollections.iiif.hymir.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.image.model.api.v2.ResizeParameters;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ResizeParametersImpl implements ResizeParameters {

  private int height;
  private int maxHeight;
  private int maxWidth;
  private int scaleFactor;
  private int width;

  public ResizeParametersImpl() {
  }

  public ResizeParametersImpl(ResizeParameters sizeParameters, float currentWidth, float currentHeight) throws InvalidParametersException {
    float aspect = (float) currentWidth / (float) currentHeight;

    int targetHeight = sizeParameters.getHeight();
    int targetWidth = sizeParameters.getWidth();
    int targetMaxHeight = sizeParameters.getMaxHeight();
    int targetMaxWidth = sizeParameters.getMaxWidth();
    scaleFactor = sizeParameters.getScaleFactor(); // given in percentage

    float calculatedHeight;
    float calculatedWidth;

    if (targetWidth > 0 && targetHeight == 0 && targetMaxHeight == 0 && targetMaxWidth == 0) {
      // only target width given, maintain aspect ratio
      calculatedWidth = targetWidth;
      calculatedHeight = calculatedWidth / aspect;
    } else if (targetWidth == 0 && targetHeight > 0 && targetMaxHeight == 0 && targetMaxWidth == 0) {
      // only target height given, maintain aspect ratio
      calculatedHeight = targetHeight;
      calculatedWidth = aspect * calculatedHeight;
    } else if (targetWidth > 0 && targetHeight > 0 && targetMaxHeight == 0 && targetMaxWidth == 0) {
      // target height and width given, aspect ratio may not be maintained!
      calculatedHeight = targetHeight;
      calculatedWidth = targetWidth;
    } else if (targetWidth == 0 && targetHeight == 0 && targetMaxHeight == 0 && targetMaxWidth == 0 && scaleFactor > 0) {
      // scale factor given
      float factor = (float) scaleFactor / 100;
      calculatedHeight = factor * currentHeight;
      calculatedWidth = factor * currentWidth;
    } else if (targetMaxHeight > 0 && targetMaxWidth > 0) {
      // max width and height given, maintain aspect ratio
      if (targetMaxHeight > currentHeight && targetMaxWidth > currentWidth) {
        // no resizing needed, both sizes are within max sizes
        calculatedHeight = -1;
        calculatedWidth = -1;
      } else if (targetMaxHeight < currentHeight && targetMaxWidth > currentWidth) {
        // only current height is out of bounds
        float factor = targetMaxHeight / currentHeight;
        calculatedHeight = factor * currentHeight;
        calculatedWidth = factor * currentWidth;
      } else if (targetMaxHeight > currentHeight && targetMaxWidth < currentWidth) {
        // only current width is out of bounds
        float factor = targetMaxWidth / currentWidth;
        calculatedHeight = factor * currentHeight;
        calculatedWidth = factor * currentWidth;
      } else {
        // width and height are out of bounds
        float factorHeight = targetMaxHeight / currentHeight;
        float factorWidth = targetMaxWidth / currentWidth;
        float factor = Math.min(factorHeight, factorWidth);
        calculatedHeight = factor * currentHeight;
        calculatedWidth = factor * currentWidth;
      }
    } else {
      throw new InvalidParametersException("Parameter set is invalid.");
    }
    width = (int) Math.ceil(calculatedWidth);
    height = (int) Math.ceil(calculatedHeight);
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public int getMaxHeight() {
    return maxHeight;
  }

  @Override
  public int getMaxWidth() {
    return maxWidth;
  }

  @Override
  public int getScaleFactor() {
    return scaleFactor;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public void setHeight(int targetHeight) {
    this.height = targetHeight;
  }

  @Override
  public void setMaxHeight(int maxHeight) {
    this.maxHeight = maxHeight;
  }

  @Override
  public void setMaxWidth(int maxWidth) {
    this.maxWidth = maxWidth;
  }

  @Override
  public void setScaleFactor(int scaleFactor) {
    this.scaleFactor = scaleFactor;
  }

  @Override
  public void setWidth(int targetWidth) {
    this.width = targetWidth;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).toString();
  }
}
