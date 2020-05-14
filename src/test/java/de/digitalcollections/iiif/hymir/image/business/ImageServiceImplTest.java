package de.digitalcollections.iiif.hymir.image.business;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

public class ImageServiceImplTest {

  ImageServiceImpl instance = new ImageServiceImpl(null, null);

  @Test
  public void testContainsAlphaChannel() throws FileNotFoundException, IOException {
    File file = ResourceUtils.getFile("classpath:test-alpha-transparency-yes.png");
    BufferedImage image = ImageIO.read(file);
    boolean expResult = true;
    boolean result = instance.containsAlphaChannel(image);
    assertEquals(expResult, result);

    file = ResourceUtils.getFile("classpath:test-alpha-transparency-no.png");
    image = ImageIO.read(file);
    expResult = false;
    result = instance.containsAlphaChannel(image);
    assertEquals(expResult, result);
  }

  @Test
  public void testContainsTransparency() throws FileNotFoundException, IOException {
    File file = ResourceUtils.getFile("classpath:test-alpha-transparency-yes.png");
    BufferedImage image = ImageIO.read(file);
    boolean expResult = true;
    boolean result = instance.containsTransparency(image);
    assertEquals(expResult, result);

    file = ResourceUtils.getFile("classpath:test-alpha-transparency-no.png");
    image = ImageIO.read(file);
    expResult = false;
    result = instance.containsTransparency(image);
    assertEquals(expResult, result);
  }
}
