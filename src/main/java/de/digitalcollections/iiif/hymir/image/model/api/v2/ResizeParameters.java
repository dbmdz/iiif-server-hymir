package de.digitalcollections.iiif.hymir.image.model.api.v2;

/**
 * Container for type safe image resizing parameters.
 */
public interface ResizeParameters {

  int getHeight();

  void setHeight(int targetHeight);

  int getMaxHeight();

  void setMaxHeight(int maxHeight);

  int getMaxWidth();

  void setMaxWidth(int maxWidth);

  int getScaleFactor();

  void setScaleFactor(int scaleFactor);

  int getWidth();

  void setWidth(int targetWidth);

}
