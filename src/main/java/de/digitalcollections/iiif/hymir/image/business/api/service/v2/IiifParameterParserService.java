package de.digitalcollections.iiif.hymir.image.business.api.service.v2;

import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.hymir.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.hymir.model.api.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.hymir.image.model.api.v2.RegionParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.ResizeParameters;
import de.digitalcollections.iiif.hymir.image.model.api.v2.RotationParameters;

public interface IiifParameterParserService {

  /**
   * Parse the target image format.
   *
   * @param targetFormat target image format (jpg, png, ...)
   * @return parsed image format
   * @throws UnsupportedFormatException if format/extension can not be parsed
   */
  ImageFormat parseIiifFormat(String targetFormat) throws UnsupportedFormatException;

  /**
   * Parse the quality parameter (determines whether the image is delivered in color, grayscale or black and white).
   *
   * @param targetQuality target image quality
   * @return parsed image quality
   * @throws InvalidParametersException if quality can not be parsed
   */
  ImageBitDepth parseIiifQuality(String targetQuality) throws InvalidParametersException;

  /**
   * The region parameter defines the rectangular portion of the full image to be returned. Region can be specified by
   * pixel coordinates, percentage or by the value “full”, which specifies that the entire image should be returned.
   *
   * <p>
   * Relative definition "pct:x,y,w,h": The region to be returned is specified as a sequence of percentages of the full
   * image’s dimensions, as reported in the Image Information document. Thus, x represents the number of pixels from the
   * 0 position on the horizontal axis, calculated as a percentage of the reported width. w represents the width of the
   * region, also calculated as a percentage of the reported width. The same applies to y and h respectively. These may
   * be floating point numbers.
   *
   * <p>
   * Absolute definition "x,y,w,h": The region of the full image to be returned is defined in terms of absolute pixel
   * values. The value of x represents the number of pixels from the 0 position on the horizontal axis. The value of y
   * represents the number of pixels from the 0 position on the vertical axis. Thus the x,y position 0,0 is the upper
   * left-most pixel of the image. w represents the width of the region and h represents the height of the region in
   * pixels.
   *
   * @param region region definition
   * @return parsed region parameters
   * @throws InvalidParametersException if region definition can not be parsed
   */
  RegionParameters parseIiifRegion(String region) throws InvalidParametersException;

  /**
   * The rotation parameter specifies mirroring and rotation. A leading exclamation mark (“!”) indicates that the image
   * should be mirrored by reflection on the vertical axis before any rotation is applied. The numerical value
   * represents the number of degrees of clockwise rotation, and may be any floating point number from 0 to 360.
   *
   * @param rotation n: The degrees of clockwise rotation from 0 up to 360., !n: The image should be mirrored and then
   * rotated.
   * @return rotation params
   * @throws InvalidParametersException if argument can not interpreted
   */
  RotationParameters parseIiifRotation(String rotation) throws InvalidParametersException;

  /**
   * Parses parameters for resizing.
   *
   * @param size target size
   * @return resize parameters
   * @throws InvalidParametersException if size can not be parsed
   */
  ResizeParameters parseIiifSize(String size) throws InvalidParametersException;

}
