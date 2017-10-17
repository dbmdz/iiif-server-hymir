package de.digitalcollections.iiif.hymir.image.model.impl.v2;

import de.digitalcollections.iiif.hymir.image.model.api.v2.RegionParameters;
import de.digitalcollections.iiif.hymir.model.api.exception.InvalidParametersException;

public class RegionParametersImpl implements RegionParameters {

  private boolean absolute;
  private float height;
  private float horizontalOffset;
  private float verticalOffset;
  private float width;

  public RegionParametersImpl() {
  }

  public RegionParametersImpl(boolean absolute, float horizontalOffset, float verticalOffset, float width, float height) {
    this.absolute = absolute;
    this.horizontalOffset = horizontalOffset;
    this.verticalOffset = verticalOffset;
    this.width = width;
    this.height = height;
  }

  /**
   * Make the parameters absolute by supplying the dimensions of the image the operation is applied to.
   * @param imageWidth
   * @param imageHeight
   * @throws InvalidParametersException
   */
  @Override
  public void makeAbsolute(int imageWidth, int imageHeight) throws InvalidParametersException {
    this.horizontalOffset = getHorizontalOffset() * imageWidth;
    this.verticalOffset = getVerticalOffset() * imageHeight;
    this.width = getWidth() * imageWidth;
    this.height = getHeight() * imageHeight;
    if (width > (imageWidth - horizontalOffset)) {
      this.width = imageWidth - horizontalOffset;
    }
    if (height > (imageHeight - verticalOffset)) {
      this.height = imageHeight - verticalOffset;
    }
    if (getHorizontalOffset() > imageWidth || getVerticalOffset() > imageHeight) {
      throw new InvalidParametersException("Either vertical or horizontal offset are outside of the image.");
    }
    this.absolute = true;
  }

  @Override
  public float getHeight() {
    return height;
  }

  @Override
  public float getHorizontalOffset() {
    return horizontalOffset;
  }

  @Override
  public float getVerticalOffset() {
    return verticalOffset;
  }

  @Override
  public float getWidth() {
    return width;
  }

  @Override
  public boolean isAbsolute() {
    return absolute;
  }

  @Override
  public void setAbsolute(boolean absolute) {
    this.absolute = absolute;
  }

  @Override
  public void setHeight(float height) {
    this.height = height;
  }

  @Override
  public void setHorizontalOffset(float horizontalOffset) {
    this.horizontalOffset = horizontalOffset;
  }

  @Override
  public void setVerticalOffset(float verticalOffset) {
    this.verticalOffset = verticalOffset;
  }

  @Override
  public void setWidth(float width) {
    this.width = width;
  }

}
