package de.digitalcollections.iiif.hymir.image.backend.impl.repository.jpegtran.v2;

public class TransformOptions {

  private int scaleX = -1;
  private int scaleY = -1;
  private int scaleQuality = -1;
  private int cropX = -1;
  private int cropY = -1;
  private int cropWidth = -1;
  private int cropHeight = -1;
  private int rotateDegree = -1;
  private boolean doHorizontalFlip = false;
  private boolean doVerticalFlip = false;
  private boolean doTranspose = false;
  private boolean doTransverse = false;
  private boolean doGrayscale = false;

  public void setScaleParameters(int width, int height, int quality) {
    this.scaleX = width;
    this.scaleY = height;
    this.scaleQuality = quality;
  }

  public void setCropParameters(int x, int y, int width, int height) {
    this.cropX = x;
    this.cropY = y;
    this.cropWidth = width;
    this.cropHeight = height;
  }

  public void setRotateDegree(int rotateDegree) {
    this.rotateDegree = rotateDegree;
  }

  public void enableHorizontalFlip() {
    this.doHorizontalFlip = true;
  }

  public void enableVerticalFlip() {
    this.doVerticalFlip = true;
  }

  public void enableTranspose() {
    this.doTranspose = true;
  }

  public void enableTransverse() {
    this.doTransverse = true;
  }

  public void enableGrayscale() {
    this.doGrayscale = true;
  }
}
