package de.digitalcollections.iiif.hymir.image.backend.impl.repository.jpegtran;

import de.digitalcollections.iiif.hymir.image.JniTest;
import de.digitalcollections.iiif.hymir.image.backend.impl.repository.jpegtran.v2.JpegImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.libjpegturbo.turbojpeg.TJException;

import static org.assertj.core.api.Assertions.assertThat;

@Category(JniTest.class)
public class JpegImageTest {

  private JpegImage image;
  private int originalHeight;
  private int originalWidth;
  private byte[] originalData;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() throws IOException {
    InputStream imgStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("test.jpg");
    this.originalData = IOUtils.toByteArray(imgStream);
    this.image = new JpegImage(originalData);
    this.originalHeight = this.image.getHeight();
    this.originalWidth = this.image.getWidth();
  }

  @Test
  public void testFromURI() throws URISyntaxException, IOException {
    URL imgUrl = Thread.currentThread()
            .getContextClassLoader()
            .getResource("test.jpg");
    JpegImage img = new JpegImage(imgUrl.toURI());
    assertThat(img.getWidth()).isEqualTo(480);
    assertThat(img.getHeight()).isEqualTo(360);
  }

  @Test
  public void testRotate() throws TJException {
    image.rotate(90).transform();
    assertThat(image.getWidth()).isEqualTo(originalHeight);

    image.rotate(180).transform();
    assertThat(image.getWidth()).isEqualTo(originalHeight);
    assertThat(image.toByteArray()).isNotEqualTo(originalData);

    image.rotate(270).transform();
    assertThat(image.getWidth()).isEqualTo(originalWidth);
    assertThat(image.toByteArray()).isNotEqualTo(originalData);

    exception.expect(IllegalArgumentException.class);
    image.rotate(-90);

    exception.expect(IllegalArgumentException.class);
    image.rotate(450);

    exception.expect(IllegalArgumentException.class);
    image.rotate(45);
  }

  @Test
  public void testFlipVertical() throws TJException {
    image.flipVertical().transform();
    assertThat(image.getHeight()).isEqualTo(originalHeight);
    assertThat(image.toByteArray()).isNotEqualTo(originalData);
  }

  @Test
  public void testFlipHorizontal() throws TJException {
    image.flipHorizontal().transform();
    assertThat(image.getHeight()).isEqualTo(originalHeight);
    assertThat(image.toByteArray()).isNotEqualTo(originalData);
  }

  @Test
  public void testTranspose() throws Exception {
    image.transpose().transform();
    assertThat(image.getHeight()).isEqualTo(originalWidth);
  }

  public void testTransverse() throws Exception {
    JpegImage transposedImage = image.transpose();
    JpegImage transversedImageH = image.transverse();
    assertThat(transversedImageH.getHeight()).isEqualTo(image.getWidth());
    assertThat(transversedImageH.toByteArray()).isNotEqualTo(image.toByteArray());
    assertThat(transversedImageH.toByteArray()).isNotEqualTo(transposedImage.toByteArray());
  }

  @Test
  public void testDownScale() throws TJException {
    JpegImage scaledImg = image.downScale(50, 50).transform();
    assertThat(scaledImg.getWidth()).isEqualTo(50);
    assertThat(scaledImg.getHeight()).isEqualTo(50);

    exception.expect(IllegalArgumentException.class);
    image.downScale(800, 800);
  }

  @Test
  public void testCrop() throws Exception {
    JpegImage croppedImage = image.crop(0, 0, 50, 50).transform();
    assertThat(croppedImage.getWidth()).isEqualTo(50);
    assertThat(croppedImage.getHeight()).isEqualTo(50);
  }

  @Test
  public void testCropFullWidth() throws Exception {
    JpegImage croppedImage = image.crop(0, 0, 480, 240).transform();
    assertThat(croppedImage.getWidth()).isEqualTo(480);
    assertThat(croppedImage.getHeight()).isEqualTo(240);
  }

  @Test
  public void testCropBadWidth() throws Exception {
    InputStream imgStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("test3.jpg");
    JpegImage image = new JpegImage(IOUtils.toByteArray(imgStream));
    JpegImage croppedImage = image.crop(0, 0, 1500, 2048).transform();
    assertThat(croppedImage.getWidth()).isEqualTo(1500);
    assertThat(croppedImage.getHeight()).isEqualTo(2048);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadData() throws Exception {
    JpegImage image = new JpegImage(new byte[]{1, 2, 3, 4, 5});
    image.downScale(50, 50);
  }

}
