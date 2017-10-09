package de.digitalcollections.iiif.hymir.image.model.api.v2;

import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.hymir.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.hymir.model.api.exception.InvalidParametersException;
import java.io.IOException;

/** NOTE: Even though this interface suggests that transformations are lazy,
 *  this is **not** binding. That is, between the first call to any of the transformation
 *  methods and the final `performTransformation` call, no assumptions can be made about the
 *  actual image data. The only guarantee is, that after having called `performTransformation`,
 *  the image has actually undergone the applied transformations.
 *  Implementors are encouraged to maintain a flag in the object's state that indicates
 *  whether a transformation is in progress (i.e. `performTransformation` has not been
 *  called yet) and raise an UnsupportedOperationException upon calling `toByteArray` if
 *  the flag is set.
 */
public interface Image {

  ImageFormat getFormat();

  int getHeight();

  int getWidth();

  byte[] toByteArray() throws UnsupportedOperationException, IOException;

  Image crop(RegionParameters params) throws UnsupportedOperationException, InvalidParametersException;

  Image scale(ResizeParameters params) throws UnsupportedOperationException, InvalidParametersException, TransformationException;

  Image rotate(int arcDegree) throws UnsupportedOperationException, InvalidParametersException;

  Image flipHorizontally();

  Image toDepth(ImageBitDepth depth) throws UnsupportedOperationException;

  Image convert(ImageFormat format) throws UnsupportedOperationException, TransformationException;

  void performTransformation() throws TransformationException;
}
