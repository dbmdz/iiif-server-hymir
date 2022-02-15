package de.digitalcollections.iiif.hymir.image.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.commons.file.business.api.FileResourceService;
import de.digitalcollections.iiif.hymir.image.business.api.ImageSecurityService;
import de.digitalcollections.iiif.hymir.model.exception.InvalidParametersException;
import de.digitalcollections.iiif.model.image.ImageApiProfile;
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
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.util.ResourceUtils;

public class ImageServiceImplTest {

  private ImageServiceImpl imageService;

  @BeforeEach
  void setUp() {
    ImageSecurityService imageSecurityService = mock(ImageSecurityService.class);
    FileResourceService fileResourceService = mock(FileResourceService.class);
    imageService = new ImageServiceImpl(imageSecurityService, fileResourceService);
  }

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
    double decodeScaleFactor = 0.12508909479686386;
    when(reader.getWidth(eq(13))).thenReturn((int) (decodeScaleFactor * nativeDimensions.width));
    when(reader.getHeight(eq(13))).thenReturn((int) (decodeScaleFactor * nativeDimensions.height));

    String identifier = "bsb00041016_00002";
    ImageApiSelector selector = new ImageApiSelector();
    selector.setIdentifier(identifier);
    selector.setRegion("full");
    selector.setSize("142,");
    selector.setRotation("90");
    selector.setQuality(Quality.DEFAULT);
    selector.setFormat(Format.JPG);

    ImageReadParam actual = ImageServiceImpl.getReadParam(reader, selector, 13);
    assertThat(actual.getSourceRegion()).isEqualTo(new Rectangle(0, 0, 351, 494));
  }

  @ParameterizedTest
  @CsvSource(
      value = {
        "/bsb12345678/full/0,0/0/default.jpg|0,0",
        "/bsb12345678/full/1,0/0/default.jpg|1,0",
        "/bsb12345678/full/0,1/0/default.jpg|0,1"
      },
      delimiter = '|')
  void shouldThrowOnInvalidSize(String request, String size) throws ResolvingException {
    ImageApiSelector selector = ImageApiSelector.fromString(request);
    ImageApiProfile imageApiProfile = mock(ImageApiProfile.class);
    OutputStream out = mock(OutputStream.class);
    assertThatExceptionOfType(InvalidParametersException.class)
        .isThrownBy(() -> imageService.processImage("bsb12345678", selector, imageApiProfile, out))
        .withMessageContaining("requested size has to be at least one pixel, but was")
        .withMessageContaining(size);
  }

  @ParameterizedTest
  @CsvSource(
      value = {
        "/bsb12345678/0,0,0,0/full/0/default.jpg|width=0.00, height=0.00",
        "/bsb12345678/0,0,1,0/full/0/default.jpg|width=1.00, height=0.00",
        "/bsb12345678/0,0,0,1/full/0/default.jpg|width=0.00, height=1.00"
      },
      delimiter = '|')
  void shouldThrowOnInvalidRegion(String request, String size) throws ResolvingException {
    ImageApiSelector selector = ImageApiSelector.fromString(request);
    ImageApiProfile imageApiProfile = mock(ImageApiProfile.class);
    OutputStream out = mock(OutputStream.class);
    assertThatExceptionOfType(InvalidParametersException.class)
        .isThrownBy(() -> imageService.processImage("bsb12345678", selector, imageApiProfile, out))
        .withMessageContaining("requested region has to have at least one pixel, but was")
        .withMessageContaining(size);
  }
}
