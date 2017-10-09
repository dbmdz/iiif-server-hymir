package de.digitalcollections.iiif.hymir.image.model.api.v2;

import de.digitalcollections.iiif.hymir.model.api.exception.InvalidParametersException;

/**
 * Container for type safe image region parameters.
 */
public interface RegionParameters {

  void makeAbsolute(int imageWidth, int imageHeight) throws InvalidParametersException;

  float getHeight();

  void setHeight(float height);

  float getHorizontalOffset();

  void setHorizontalOffset(float horizontalOffset);

  float getVerticalOffset();

  void setVerticalOffset(float verticalOffset);

  float getWidth();

  void setWidth(float width);

  boolean isAbsolute();

  void setAbsolute(boolean absolute);

}
