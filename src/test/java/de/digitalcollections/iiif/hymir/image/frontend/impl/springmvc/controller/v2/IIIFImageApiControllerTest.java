package de.digitalcollections.iiif.hymir.image.frontend.impl.springmvc.controller.v2;

import com.twelvemonkeys.imageio.stream.ByteArrayImageInputStream;
import de.digitalcollections.iiif.hymir.Application;
import de.digitalcollections.iiif.hymir.image.backend.impl.repository.imageio.v2.JAIImage;
import de.digitalcollections.iiif.hymir.image.backend.impl.repository.jpegtran.v2.JpegTranImage;
import de.digitalcollections.iiif.hymir.model.api.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.hymir.image.model.api.v2.Image;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, TestConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IIIFImageApiControllerTest {

  @Autowired
  protected IIIFImageApiController iiifController;

  private MockMvc mockMvc;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired
  protected WebApplicationContext wac;

  @BeforeClass
  public static void beforeClass() {
    System.setProperty("spring.profiles.active", "TEST");
  }

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void getFedoraInfo() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/info.json"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.width").value(2064))
            .andExpect(jsonPath("$.height").value(2553));
  }

  @Test
  public void getJsonLd() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/info.json")
            .header("Referer", "http://localhost/foobar")
            .header("Accept", "application/ld+json"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/ld+json"));
  }

  /* 5. Information Request */
  @Test
  public void getZendInfo() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/http-bsb/info.json").header("Host", "localhost"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(header().string("Link", "<http://iiif.io/api/image/2/context.json>; "
                                       + "rel=\"http://www.w3.org/ns/json-ld#context\"; "
                                       + "type=\"application/ld+json\""))
            .andExpect(jsonPath("$.width").value(989))
            .andExpect(jsonPath("$.height").value(1584))
            .andExpect(jsonPath("$.@context").value("http://iiif.io/api/image/2/context.json"))
            .andExpect(jsonPath("$.@id").value("http://localhost/image/" + IIIFImageApiController.VERSION + "/http-bsb"))
            .andExpect(jsonPath("$.protocol").value("http://iiif.io/api/image"))
            .andExpect(jsonPath("$.profile[0]").value("http://iiif.io/api/image/2/level2.json"))
            .andExpect(jsonPath("$.tiles.length()").value(3))
            .andExpect(jsonPath("$.tiles[0].width").value(128));
  }

  @Test
  public void getInfoRedirect() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/abcdef"))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.redirectedUrl("/image/" + IIIFImageApiController.VERSION + "/abcdef/info.json"));
  }

  private Image loadImage(byte[] imageData, boolean useFast) throws IOException, UnsupportedFormatException {
    Image image;
    if (!useFast) {
      try {
        return new JAIImage(imageData);
      } catch (de.digitalcollections.iiif.hymir.model.api.exception.UnsupportedFormatException ex) {
        throw new UnsupportedFormatException(ex.getMessage());
      }
    } else {
      try {
        image = new JpegTranImage(imageData);
      } catch (Throwable e) {
        try {
          image = new JAIImage(imageData);
        } catch (de.digitalcollections.iiif.hymir.model.api.exception.UnsupportedFormatException ex) {
          throw new UnsupportedFormatException(ex.getMessage());
        }
      }
      return image;
    }
  }

  @Test
  public void testBinarization() throws Exception {
    byte[] imgData = mockMvc
            .perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/0,0,1500,2048/750,/90/bitonal.png").header("Referer", "http://localhost/foobar"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    BufferedImage image = ((JAIImage) loadImage(imgData, false)).getImage();
    Assert.assertEquals(BufferedImage.TYPE_BYTE_BINARY, image.getType());
  }

  @Test
  public void testContentDispositionHeader() throws Exception {
    mockMvc
            .perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/full/full/0/default.png").header("Referer", "http://localhost/foobar"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/png"))
            .andExpect(header().string("Content-Disposition", "inline; filename=" + IIIFImageApiController.VERSION + "_http-google_full_full_0_default.png"));
  }

  /* 4.5 Format */
  @Test
  public void testConvertPng() throws Exception {
    byte[] imgData = mockMvc
            .perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/full/full/0/default.png").header("Referer", "http://localhost/foobar"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/png"))
            .andReturn().getResponse().getContentAsByteArray();
    ImageInputStream iis = new ByteArrayImageInputStream(imgData);
    Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
    while (readers.hasNext()) {
      ImageReader reader = (ImageReader) readers.next();
      Assert.assertEquals("png", reader.getFormatName().toLowerCase());
    }
  }

  /* Other */
  @Test
  public void testCorsHeader() throws Exception {
    // NOTE: The spec actually recommends to return '*' as the value for the CORS header, but Spring always
    // matches the 'Origin' header of the request. While this goes against the recommendation, the outcome
    // (i.e. everybody can use the API) is the same.
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/info.json").header("Origin", "http://im.a.foreign.er"))
            .andExpect(header().string("Access-Control-Allow-Origin", "http://im.a.foreign.er"));
  }

  @Test
  public void testNonStandardPort() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/info.json").header("Host", "example.com:8080"))
            .andExpect(jsonPath("$.@id").value("http://example.com:8080/image/" + IIIFImageApiController.VERSION + "/http-google"));

  }

  @Test
  public void testXForwardedProto() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/info.json").header("Host", "localhost").header("X-Forwarded-Proto", "https"))
            .andExpect(jsonPath("$.@id").value("https://localhost/image/" + IIIFImageApiController.VERSION + "/http-google"));
  }

  @Test
  public void testXForwardedHost() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/info.json").header("X-Forwarded-Host", "example.org"))
            .andExpect(jsonPath("$.@id").value("http://example.org/image/" + IIIFImageApiController.VERSION + "/http-google"));
  }

  @Test
  public void testXForwardedHostWithPort() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/info.json").header("X-Forwarded-Host", "example.org:8080"))
            .andExpect(jsonPath("$.@id").value("http://example.org:8080/image/" + IIIFImageApiController.VERSION + "/http-google"));
  }


  /* 4.1 Region */
  @Test
  public void testCropWithAbsoluteValues() throws Exception {
    byte[] imgData = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/20,20,50,50/full/0/native.jpg"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    Image image = loadImage(imgData, true);
    Assert.assertEquals(50, image.getHeight());
    Assert.assertEquals(50, image.getWidth());
  }

  @Test
  public void testCropWithAbsoluteValuesTotallyExceeding() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/5000,5000,100,100/full/0/native.jpg"))
            .andExpect(status().is((400)));
  }

  @Test
  public void testCropWithRelativeValues() throws Exception {
    byte[] imgData = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/pct:10,10,10,10/full/0/native.jpg"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    Image image = loadImage(imgData, true);
    Assert.assertEquals(256, image.getHeight());
    Assert.assertEquals(207, image.getWidth());
  }

  @Test
  public void testCropWithRelativeValuesPartiallyExceeding() throws Exception {
    byte[] imgData = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/pct:20,20,100,10/full/0/native.jpg"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    Image image = loadImage(imgData, true);
    Assert.assertEquals(256, image.getHeight());
    Assert.assertEquals(1651, image.getWidth());
  }

  /* 4.4 Quality */
  @Test
  public void testGrayscaling() throws Exception {
    byte[] imgData = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/0,0,1500,2048/750,/90/gray.jpg").header("Referer", "http://localhost/foobar"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    BufferedImage image = ((JAIImage) loadImage(imgData, false)).getImage();
    Raster ras = image.getRaster();
    Assert.assertEquals(1, ras.getNumDataElements());
    Assert.assertEquals(BufferedImage.TYPE_BYTE_GRAY, image.getType());
  }

  @Test
  public void testMirror() throws Exception {
    byte[] imgDataRegular = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/0,0,1500,2048/750,/0/native.jpg"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    byte[] imgDataMirror = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/0,0,1500,2048/750,/!0/native.jpg"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    Image regularImage = loadImage(imgDataRegular, true);
    Image mirroredImage = loadImage(imgDataMirror, true);
    Assert.assertEquals(regularImage.getWidth(), mirroredImage.getWidth());
    Assert.assertEquals(regularImage.getHeight(), mirroredImage.getHeight());
    Assert.assertThat(regularImage.toByteArray(), Matchers.not(Matchers.equalTo((mirroredImage.toByteArray()))));
  }

  /* 4.3 Rotation */
  @Test
  public void testRotation() throws Exception {
    byte[] imgData = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/0,0,1500,2048/750,/90/native.jpg"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    Image image = loadImage(imgData, true);
    Assert.assertEquals(750, image.getHeight());
    Assert.assertEquals(1024, image.getWidth());
  }

  @Test
  public void testScaleWithBestWidth() throws Exception {
    byte[] imgData = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/full/!500,500/0/native.jpg"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    Image image = loadImage(imgData, true);
    Assert.assertThat(image.getWidth(), Matchers.lessThanOrEqualTo(500));
    Assert.assertThat(image.getHeight(), Matchers.lessThanOrEqualTo(500));
  }

  @Test
  public void testScaleWithMissingHeight() throws Exception {
    byte[] imgData = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/full/,200/0/native.jpg"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    Image image = loadImage(imgData, true);
    Assert.assertEquals(200, image.getHeight());
  }

  /* 4.2 Size */
  @Test
  public void testScaleWithMissingWidth() throws Exception {
    byte[] imgData = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/full/200,/0/native.jpg"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    Image image = loadImage(imgData, true);
    Assert.assertEquals(200, image.getWidth());
  }

  @Test
  public void testScaleWithRelativeValues() throws Exception {
    byte[] imgData = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/full/pct:50/0/native.jpg"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    Image image = loadImage(imgData, true);
    Assert.assertEquals(1032, image.getWidth());
    Assert.assertEquals(1277, image.getHeight());
  }

  @Test
  public void testScaleWithZeroSize() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/full/!500,0/0/native.jpg"))
            .andExpect(status().is(400));
  }

  @Test
  public void testRelativeCropWithAbsoluteScale() throws Exception {
    byte[] imgData = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/pct:10,20,20,20/500,/0/native.jpg"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    Image image = loadImage(imgData, true);
    Assert.assertTrue(image.getWidth() != image.getHeight());
  }

  @Test
  public void testUrlEncodedIdentifiers() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/" + URLEncoder.encode("spec:/ial?file#with[special]ch@arac%ters", "utf8") + "/info.json")
            .header("Host", "localhost")
            .header("Referer", "http://localhost/foobar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.@id").value("http://localhost/image/" + IIIFImageApiController.VERSION + "/spec%253A%252Fial%253Ffile%2523with%255Bspecial%255Dch%2540arac%2525ters"));
  }
}
