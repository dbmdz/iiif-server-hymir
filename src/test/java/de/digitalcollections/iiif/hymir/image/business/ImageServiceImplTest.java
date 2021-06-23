package de.digitalcollections.iiif.hymir.image.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.iiif.hymir.model.exception.InvalidParametersException;
import de.digitalcollections.iiif.model.image.ImageApiProfile.Format;
import de.digitalcollections.iiif.model.image.ImageApiProfile.Quality;
import de.digitalcollections.iiif.model.image.ImageApiSelector;
import de.digitalcollections.iiif.model.image.ResolvingException;
import de.digitalcollections.turbojpeg.imageio.TurboJpegImageReadParam;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

public class ImageServiceImplTest {

  @Test
  public void testContainsAlphaChannel() throws IOException {
    File file = ResourceUtils.getFile("classpath:test-alpha-transparency-yes.png");
    BufferedImage image = ImageIO.read(file);
    boolean expResult = true;
    boolean result = ImageServiceImpl.containsAlphaChannel(image);
    assertEquals(expResult, result);

    file = ResourceUtils.getFile("classpath:test-alpha-transparency-no.png");
    image = ImageIO.read(file);
    expResult = false;
    result = ImageServiceImpl.containsAlphaChannel(image);
    assertEquals(expResult, result);
  }

  @Test
  void getReadParamShouldRoundCorrectly()
      throws ResolvingException, InvalidParametersException, IOException {
    ImageReader reader = mock(ImageReader.class);
    when(reader.getDefaultReadParam()).thenAnswer(invocation -> new TurboJpegImageReadParam());
    Dimension nativeDimensions = new Dimension(2806, 3952);
    when(reader.getWidth(eq(0))).thenReturn(nativeDimensions.width);
    when(reader.getHeight(eq(0))).thenReturn(nativeDimensions.height);

    String identifier = "bsb00041016_00002";
    ImageApiSelector selector = new ImageApiSelector();
    selector.setIdentifier(identifier);
    selector.setRegion("full");
    selector.setSize("142,");
    selector.setRotation("90");
    selector.setQuality(Quality.DEFAULT);
    selector.setFormat(Format.JPG);

    ImageReadParam actual = ImageServiceImpl.getReadParam(reader, selector, 0.12508909479686386);
    assertThat(actual.getSourceRegion()).isEqualTo(new Rectangle(0, 0, 351, 494));
  }
}
