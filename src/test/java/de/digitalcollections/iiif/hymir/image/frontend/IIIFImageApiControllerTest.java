package de.digitalcollections.iiif.hymir.image.frontend;

import com.twelvemonkeys.imageio.stream.ByteArrayImageInputStream;
import de.digitalcollections.iiif.hymir.Application;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
  public void testImageInfoSizes() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/info.json"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.width").value(2064))
            .andExpect(jsonPath("$.height").value(2553));
  }

  @Test
  public void testImageInfoContentType() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/info.json")
            .header("Referer", "http://localhost/foobar")
            .header("Accept", "application/ld+json"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/ld+json"));
  }

  /* 5. Information Request */
  @Test
  public void testImageInfo() throws Exception {
    MvcResult result = mockMvc.perform(
        get("/image/" + IIIFImageApiController.VERSION + "/http-bsb/info.json")
        .header("Host", "localhost"))
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
        .andExpect(jsonPath("$.tiles.length()").value(1))
        .andExpect(jsonPath("$.tiles[0].width").value(512))
        .andReturn();
    assertThat(result.getResponse().getDateHeader("Last-Modified")).isNotNull();
  }

  @Test
  public void getInfoRedirect() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/abcdef"))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.redirectedUrl("/image/" + IIIFImageApiController.VERSION + "/abcdef/info.json"));
  }

  private BufferedImage loadImage(byte[] imageData) throws IOException {
    return ImageIO.read(new ByteArrayInputStream(imageData));
  }

  @Test
  public void testBinarization() throws Exception {
    byte[] imgData = mockMvc
            .perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/0,0,1500,2048/750,/90/bitonal.png").header("Referer", "http://localhost/foobar"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getType()).isEqualTo(BufferedImage.TYPE_BYTE_BINARY);
  }

  @Test
  public void testContentDispositionHeader() throws Exception {
    MvcResult result = mockMvc.perform(
        get("/image/" + IIIFImageApiController.VERSION + "/http-google/full/full/0/default.png").header("Referer", "http://localhost/foobar"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("image/png"))
        .andExpect(header().string("Content-Disposition", "inline; filename=" + IIIFImageApiController.VERSION + "_http-google_full_full_0_default.png"))
        .andReturn();
    assertThat(result.getResponse().getDateHeader("Last-Modified")).isNotNull();
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
      ImageReader reader = readers.next();
      assertThat(reader.getFormatName()).isEqualToIgnoringCase("png");
    }
  }

  /* Other */
  @Test
  public void testCorsHeader() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/info.json").header("Origin", "http://im.a.foreign.er"))
            .andExpect(header().string("Access-Control-Allow-Origin", "*"));
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
    BufferedImage image = loadImage(imgData);
    assertThat(image.getWidth()).isEqualTo(50);
    assertThat(image.getHeight()).isEqualTo(50);
  }

  @Test
  public void testCropWithAbsoluteValuesTotallyExceeding() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/5000,5000,100,100/full/0/native.jpg"))
            .andExpect(status().is((400)));
  }

  @Test
  public void testCropWithRelativeValues() throws Exception {
    String location = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/pct:10,10,10,10/full/0/native.jpg"))
        .andExpect(status().is3xxRedirection())
        .andReturn().getResponse().getHeader("Location");
    byte[] imgData = mockMvc.perform(get(location))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getWidth()).isEqualTo(206);
    assertThat(image.getHeight()).isEqualTo(255);
  }

  @Test
  public void testCropWithRelativeValuesPartiallyExceeding() throws Exception {
    String location = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/pct:20,20,100,10/full/0/native.jpg"))
        .andExpect(status().is3xxRedirection())
        .andReturn().getResponse().getHeader("Location");
    byte[] imgData = mockMvc.perform(get(location))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getHeight()).isEqualTo(255);
    assertThat(image.getWidth()).isEqualTo(1651);
  }

  @Test
  public void testCropWithSquareLargerWidth() throws Exception {
    String location = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/square-width/square/full/0/native.jpg"))
        .andExpect(status().is3xxRedirection())
        .andReturn().getResponse().getHeader("Location");
    byte[] imgData = mockMvc.perform(get(location))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getHeight()).isEqualTo(219);
    assertThat(image.getWidth()).isEqualTo(219);
  }

  @Test
  public void testCropWithSquareLargerHeight() throws Exception {
    String location = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/square-height/square/full/0/native.jpg"))
        .andExpect(status().is3xxRedirection())
        .andReturn().getResponse().getHeader("Location");
    byte[] imgData = mockMvc.perform(get(location))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getHeight()).isEqualTo(249);
    assertThat(image.getWidth()).isEqualTo(249);
  }

  @Test
  public void testCropWithSquareAlreadySquare() throws Exception {
    String location = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/square/square/full/0/native.jpg"))
        .andExpect(status().is3xxRedirection())
        .andReturn().getResponse().getHeader("Location");
    byte[] imgData = mockMvc.perform(get(location))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getHeight()).isEqualTo(350);
    assertThat(image.getWidth()).isEqualTo(350);
  }

  /* 4.4 Quality */
  @Test
  public void testGrayscaling() throws Exception {
    byte[] imgData = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/0,0,1500,2048/750,/90/gray.jpg").header("Referer", "http://localhost/foobar"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    BufferedImage image = loadImage(imgData);
    Raster ras = image.getRaster();
    assertThat(ras.getNumDataElements()).isEqualTo(1);
    assertThat(image.getType()).isEqualTo(BufferedImage.TYPE_BYTE_GRAY);
  }

  @Test
  public void testMirror() throws Exception {
    byte[] imgDataRegular = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/0,0,1500,2048/750,/0/native.jpg"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    byte[] imgDataMirror = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/0,0,1500,2048/750,/!0/native.jpg"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    BufferedImage regularImage = loadImage(imgDataRegular);
    BufferedImage mirroredImage = loadImage(imgDataMirror);
    assertThat(mirroredImage.getWidth()).isEqualTo(regularImage.getWidth());
    assertThat(mirroredImage.getHeight()).isEqualTo(regularImage.getHeight());
    assertThat(imgDataRegular).isNotEqualTo(imgDataMirror);
  }

  /* 4.3 Rotation */
  @Test
  public void testRotation() throws Exception {
    byte[] imgData = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/http-google/0,0,1500,2048/750,/90/native.jpg"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getWidth()).isEqualTo(1024);
    assertThat(image.getHeight()).isEqualTo(750);
  }

  @Test
  public void testScaleWithBestWidth() throws Exception {
    String location = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/full/!500,500/0/native.jpg"))
        .andExpect(status().is3xxRedirection())
        .andReturn().getResponse().getHeader("Location");
    byte[] imgData = mockMvc.perform(get(location))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getWidth()).isLessThanOrEqualTo(500);
    assertThat(image.getHeight()).isLessThanOrEqualTo(500);
  }

  @Test
  public void testScaleWithMissingWidth() throws Exception {
    String location = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/full/,200/0/native.jpg"))
        .andExpect(status().is3xxRedirection())
        .andReturn().getResponse().getHeader("Location");
    byte[] imgData = mockMvc.perform(get(location))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getHeight()).isEqualTo(200);
  }

  /* 4.2 Size */
  @Test
  public void testScaleWithMissingHeigth() throws Exception {
    byte[] imgData = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/full/200,/0/native.jpg"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getWidth()).isEqualTo(200);
  }

  @Test
  public void testScaleWithRelativeValues() throws Exception {
    String location = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/full/pct:50/0/native.jpg"))
        .andExpect(status().is3xxRedirection())
        .andReturn().getResponse().getHeader("Location");
    byte[] imgData = mockMvc.perform(get(location))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getWidth()).isEqualTo(1032);
    assertThat(image.getHeight()).isEqualTo(1276);
  }

  @Test
  public void testRelativeCropWithAbsoluteScale() throws Exception {
    String location = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/pct:10,20,20,20/500,/0/native.jpg"))
        .andExpect(status().is3xxRedirection())
        .andReturn().getResponse().getHeader("Location");
    byte[] imgData = mockMvc.perform(get(location))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getWidth()).isEqualTo(500);
    assertThat(image.getHeight()).isEqualTo(618);
  }

  @Test
  public void testUrlEncodedIdentifiers() throws Exception {
    mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/" + URLEncoder.encode("spec:/ial?file#with[special]ch@arac%ters", "utf8") + "/info.json")
            .header("Host", "localhost")
            .header("Referer", "http://localhost/foobar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.@id").value("http://localhost/image/" + IIIFImageApiController.VERSION + "/spec%253A%252Fial%253Ffile%2523with%255Bspecial%255Dch%2540arac%2525ters"));
  }

  @Test
  public void testCanonicalRedirectWithRelativeCropAndScale() throws Exception {
    String location = mockMvc.perform(get("/image/" + IIIFImageApiController.VERSION + "/file-zoom/pct:10,20,20,20/pct:84/0/native.jpg"))
        .andExpect(status().is3xxRedirection())
        .andReturn().getResponse().getHeader("Location");
    assertThat(location).isEqualTo("http://localhost/image/v2/file-zoom/206,511,413,511/346,/0/default.jpg");
  }
}
