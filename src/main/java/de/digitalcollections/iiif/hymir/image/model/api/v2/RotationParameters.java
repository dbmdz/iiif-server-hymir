package de.digitalcollections.iiif.hymir.image.model.api.v2;

/**
 * Container for type safe image region parameters.
 */
public interface RotationParameters {

  /**
   * @return The degrees of clockwise rotation from 0 up to 360.
   */
  int getDegrees();

  void setDegrees(int degrees);

  boolean isMirrorHorizontally();

  void setMirrorHorizontally(boolean mirrorHorizontally);

}
