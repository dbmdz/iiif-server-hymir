package de.digitalcollections.iiif.hymir.image.model.impl.v2;

import de.digitalcollections.iiif.hymir.image.model.api.v2.RotationParameters;

public class RotationParametersImpl implements RotationParameters {

  private int degrees;
  private boolean mirrorHorizontally;

  @Override
  public int getDegrees() {
    return degrees;
  }

  @Override
  public void setDegrees(int degrees) {
    this.degrees = degrees;
  }

  @Override
  public boolean isMirrorHorizontally() {
    return mirrorHorizontally;
  }

  @Override
  public void setMirrorHorizontally(boolean mirrorHorizontally) {
    this.mirrorHorizontally = mirrorHorizontally;
  }
}
