package de.digitalcollections.iiif.hymir.image.frontend;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.twelvemonkeys.imageio.stream.ByteArrayImageInputStream;
import de.digitalcollections.iiif.hymir.Application;
import de.digitalcollections.iiif.hymir.TestConfiguration;
import de.digitalcollections.iiif.hymir.image.business.ImageServiceImpl;
import de.digitalcollections.turbojpeg.lib.libturbojpeg;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Runtime;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;

import static de.digitalcollections.iiif.hymir.HymirAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    properties = {"spring.profiles.active=TEST",
                  "spring.config.name=application-test"},
    classes = {Application.class, TestConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IIIFImageApiControllerTest {

  static final Condition<HttpStatus> BAD_REQUEST = new Condition<>(value -> value.value() == 400, "400 BAD_REQUEST");

  @LocalServerPort
  int randomServerPort;

  @Autowired
  protected IIIFImageApiController iiifController;

  @Autowired
  protected ImageServiceImpl imageService;

  @Autowired
  private TestRestTemplate restTemplate;

  @BeforeAll
  public static void beforeAll() {
    System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
    TestConfiguration.setDefaults();
  }

  @Tag("libturbojpeg-needed")
  @Test
  public void testTurboJpegInstalled() {
    libturbojpeg lib = LibraryLoader.create(libturbojpeg.class)
                                    .load("turbojpeg");
    Runtime runtime = Runtime.getRuntime(lib);

    assertThat(runtime).isNotNull();
  }

  @Test
  public void testImageInfoSizes() throws Exception {
    ResponseEntity<String> response = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/file-zoom/info.json", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

    DocumentContext ctx = JsonPath.parse(response.getBody());
    assertThat(ctx).jsonPathAsInteger("$.width").isEqualTo(2064);
    assertThat(ctx).jsonPathAsInteger("$.height").isEqualTo(2553);
  }

  @Test
  public void testImageInfoContentType() throws Exception {
    ResponseEntity<String> response = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/file-zoom/info.json", String.class);
//    assertThat(response.getHeaders().get("Referer")).isEqualTo("http://localhost/foobar"); // referer is null...
//    assertThat(response.getHeaders().getAccept()).contains(MediaType.parseMediaType("application/ld+json")); // accept is null
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
  }

  /* 5. Information Request */
  @Tag("libturbojpeg-needed")
  @Test
  public void testImageInfo() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("Host", "localhost");
    ResponseEntity<String> response = restTemplate.exchange("/image/" + IIIFImageApiController.VERSION + "/http-bsb/info.json", HttpMethod.GET, new HttpEntity<>(requestHeaders), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getLastModified()).isNotNull();
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    assertThat(response.getHeaders().get("header1")).containsExactly("value1");

    assertThat(response.getHeaders().get("Link")).containsExactly(
        "<http://iiif.io/api/image/2/context.json>; rel=\"http://www.w3.org/ns/json-ld#context\"; type=\"application/ld+json\"",
        "<http://iiif.io/api/image/2/level2.json>;rel=\"profile\"");
    DocumentContext ctx = JsonPath.parse(response.getBody());
    assertThat(ctx).jsonPathAsInteger("$.width").isEqualTo(989);
    assertThat(ctx).jsonPathAsInteger("$.height").isEqualTo(1584);
    assertThat(ctx).jsonPathAsString("$.@context").isEqualTo("http://iiif.io/api/image/2/context.json");
    assertThat(ctx).jsonPathAsString("$.@id").isEqualTo("http://localhost/image/" + IIIFImageApiController.VERSION + "/http-bsb");
    assertThat(ctx).jsonPathAsString("$.protocol").isEqualTo("http://iiif.io/api/image");
    assertThat(ctx).jsonPathAsString("$.profile[0]").isEqualTo("http://iiif.io/api/image/2/level2.json");
    assertThat(ctx).jsonPathAsInteger("$.tiles.length()").isEqualTo(1);
    assertThat(ctx).jsonPathAsInteger("$.tiles[0].width").isEqualTo(512);
    assertThat(ctx).jsonPathAsString("$.profile[0]").isEqualTo("http://iiif.io/api/image/2/level2.json");

    assertThatThrownBy(() -> ctx.read("$.profile[1].formats.qualities"))
        .isInstanceOf(JsonPathException.class);
    assertThat(ctx).jsonPathAsListOf("$.profile[1].formats", String.class).isNotEmpty();
  }

  @Test
  public void testInfoLicenseInformation() throws Exception {
    imageService.setAttribution("Test Attribution");
    imageService.setLogoUrl("https://example.com/logo.jpg");
    ResponseEntity<String> response = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/file-zoom/info.json", String.class);
    DocumentContext ctx = JsonPath.parse(response.getBody());
    assertThat(ctx).jsonPathAsString("$.license").isEqualTo("https://example.com/my-license");
    assertThat(ctx).jsonPathAsString("$.logo['@id']").isEqualTo("https://example.com/logo.jpg");
    assertThat(ctx).jsonPathAsString("$.attribution").isEqualTo("Test Attribution");
  }

  @Test
  public void testInfoRedirect() throws Exception {
    ResponseEntity<String> response = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/abcdef", String.class);
    assertThat(response.getStatusCode().series()).isEqualTo(HttpStatus.Series.REDIRECTION);
    assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/image/" + IIIFImageApiController.VERSION + "/abcdef/info.json");
  }

  private BufferedImage loadImage(byte[] imageData) throws IOException {
    return ImageIO.read(new ByteArrayInputStream(imageData));
  }

  @Test
  public void testBinarization() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("Referer", "http://localhost/foobar");
    ResponseEntity<byte[]> response = restTemplate.exchange(
        "/image/" + IIIFImageApiController.VERSION + "/http-google/0,0,1500,2048/750,/90/bitonal.png",
        HttpMethod.GET, new HttpEntity<>(requestHeaders), byte[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    byte[] imgData = response.getBody();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getType()).isEqualTo(BufferedImage.TYPE_BYTE_BINARY);
  }

  @Test
  public void testContentDispositionHeader() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("Referer", "http://localhost/foobar");
    ResponseEntity<byte[]> response = restTemplate.exchange(
        "/image/" + IIIFImageApiController.VERSION + "/http-google/full/full/0/default.png",
        HttpMethod.GET, new HttpEntity<>(requestHeaders), byte[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getLastModified()).isNotNull();
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_PNG);
    assertThat(response.getHeaders().getContentDisposition().getType()).isEqualTo("inline");
    assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo(IIIFImageApiController.VERSION + "_http-google_full_full_0_default.png");
    assertThat(response.getHeaders().get("cache-control")).containsExactly("max-age=86400");
  }

  /* 4.5 Format */
  @Test
  public void testConvertPng() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("Referer", "http://localhost/foobar");
    ResponseEntity<byte[]> response = restTemplate.exchange(
        "/image/" + IIIFImageApiController.VERSION + "/http-google/full/full/0/default.png",
        HttpMethod.GET, new HttpEntity<>(requestHeaders), byte[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_PNG);

    byte[] imgData = response.getBody();
    ImageInputStream iis = new ByteArrayImageInputStream(imgData);
    Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
    while (readers.hasNext()) {
      ImageReader reader = readers.next();
      assertThat(reader.getFormatName().toLowerCase()).isEqualTo("png");
    }
  }

  /* Other */
  @Test
  public void testCorsHeader() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setOrigin("http://im.a.foreign.er");
    ResponseEntity<String> response = restTemplate.exchange(
        "/image/" + IIIFImageApiController.VERSION + "/http-google/info.json",
        HttpMethod.GET, new HttpEntity<>(requestHeaders), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getAccessControlAllowOrigin()).isEqualTo("*");
  }

  @Test
  public void testNonStandardPort() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("Host", "example.com:8080");
    ResponseEntity<String> response = restTemplate.exchange("/image/" + IIIFImageApiController.VERSION + "/http-google/info.json", HttpMethod.GET, new HttpEntity<>(requestHeaders), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    DocumentContext ctx = JsonPath.parse(response.getBody());
    assertThat(ctx).jsonPathAsString("$.@id").isEqualTo("http://example.com:8080/image/" + IIIFImageApiController.VERSION + "/http-google");
  }

  @Test
  public void testXForwardedProto() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("Host", "localhost");
    requestHeaders.add("X-Forwarded-Proto", "https");
    ResponseEntity<String> response = restTemplate.exchange("/image/" + IIIFImageApiController.VERSION + "/http-google/info.json", HttpMethod.GET, new HttpEntity<>(requestHeaders), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    DocumentContext ctx = JsonPath.parse(response.getBody());
    assertThat(ctx).jsonPathAsString("$.@id").isEqualTo("https://localhost/image/" + IIIFImageApiController.VERSION + "/http-google");
  }

  @Test
  public void testXForwardedHost() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("X-Forwarded-Host", "example.org");
    ResponseEntity<String> response = restTemplate.exchange("/image/" + IIIFImageApiController.VERSION + "/http-google/info.json", HttpMethod.GET, new HttpEntity<>(requestHeaders), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    DocumentContext ctx = JsonPath.parse(response.getBody());
    assertThat(ctx).jsonPathAsString("$.@id").isEqualTo("http://example.org/image/" + IIIFImageApiController.VERSION + "/http-google");
  }

  @Test
  public void testXForwardedHostWithPort() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("X-Forwarded-Host", "example.org:8080");
    ResponseEntity<String> response = restTemplate.exchange("/image/" + IIIFImageApiController.VERSION + "/http-google/info.json", HttpMethod.GET, new HttpEntity<>(requestHeaders), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    DocumentContext ctx = JsonPath.parse(response.getBody());
    assertThat(ctx).jsonPathAsString("$.@id").isEqualTo("http://example.org:8080/image/" + IIIFImageApiController.VERSION + "/http-google");
  }

  /* 4.1 Region */
  @Test
  public void testCropWithAbsoluteValues() throws Exception {
    ResponseEntity<byte[]> response = restTemplate.exchange(
        "/image/" + IIIFImageApiController.VERSION + "/file-zoom/20,20,50,50/full/0/default.jpg",
        HttpMethod.GET, HttpEntity.EMPTY, byte[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    byte[] imgData = response.getBody();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getHeight()).isEqualTo(50);
    assertThat(image.getWidth()).isEqualTo(50);
  }

  @Test
  public void testCropWithAbsoluteValuesTotallyExceeding() throws Exception {
    ResponseEntity<String> response = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/file-zoom/5000,5000,100,100/full/0/default.jpg", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void testCropWithRelativeValues() throws Exception {
    ResponseEntity<String> response1 = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/file-zoom/pct:10,10,10,10/full/0/default.jpg", String.class);
    assertThat(response1.getStatusCode().series()).isEqualTo(HttpStatus.Series.REDIRECTION);
    String location = response1.getHeaders().getLocation().getPath();
    assertThat(location).isEqualTo("/image/" + IIIFImageApiController.VERSION + "/file-zoom/206,255,206,255/full/0/default.jpg");

    ResponseEntity<byte[]> response2 = restTemplate.exchange(location, HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
    byte[] imgData = response2.getBody();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getHeight()).isEqualTo(255);
    assertThat(image.getWidth()).isEqualTo(206);
  }

  @Test
  public void testCropWithRelativeValuesPartiallyExceeding() throws Exception {
    ResponseEntity<String> response1 = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/file-zoom/pct:20,20,100,10/full/0/default.jpg", String.class);
    assertThat(response1.getStatusCode().series()).isEqualTo(HttpStatus.Series.REDIRECTION);
    String location = response1.getHeaders().getLocation().getPath();
    assertThat(location).isEqualTo("/image/" + IIIFImageApiController.VERSION + "/file-zoom/413,511,1651,255/full/0/default.jpg");

    ResponseEntity<byte[]> response2 = restTemplate.exchange(location, HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
    byte[] imgData = response2.getBody();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getHeight()).isEqualTo(255);
    assertThat(image.getWidth()).isEqualTo(1651);
  }

  @Test
  public void testCropWithSquareLargerWidth() throws Exception {
    ResponseEntity<String> response1 = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/square-width/square/full/0/default.jpg", String.class);
    assertThat(response1.getStatusCode().series()).isEqualTo(HttpStatus.Series.REDIRECTION);
    String location = response1.getHeaders().getLocation().getPath();
    assertThat(location).isEqualTo("/image/" + IIIFImageApiController.VERSION + "/square-width/122,0,219,219/full/0/default.jpg");

    ResponseEntity<byte[]> response2 = restTemplate.exchange(location, HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
    byte[] imgData = response2.getBody();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getHeight()).isEqualTo(219);
    assertThat(image.getWidth()).isEqualTo(219);
  }

  @Test
  public void testCropWithSquareLargerHeight() throws Exception {
    ResponseEntity<String> response1 = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/square-height/square/full/0/default.jpg", String.class);
    assertThat(response1.getStatusCode().series()).isEqualTo(HttpStatus.Series.REDIRECTION);
    String location = response1.getHeaders().getLocation().getPath();
    assertThat(location).isEqualTo("/image/" + IIIFImageApiController.VERSION + "/square-height/0,40,249,249/full/0/default.jpg");

    ResponseEntity<byte[]> response2 = restTemplate.exchange(location, HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
    byte[] imgData = response2.getBody();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getHeight()).isEqualTo(249);
    assertThat(image.getWidth()).isEqualTo(249);
  }

  @Test
  public void testCropWithSquareAlreadySquare() throws Exception {
    ResponseEntity<String> response1 = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/square/square/full/0/default.jpg", String.class);
    assertThat(response1.getStatusCode().series()).isEqualTo(HttpStatus.Series.REDIRECTION);
    String location = response1.getHeaders().getLocation().getPath();
    assertThat(location).isEqualTo("/image/" + IIIFImageApiController.VERSION + "/square/full/full/0/default.jpg");

    ResponseEntity<byte[]> response2 = restTemplate.exchange(location, HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
    byte[] imgData = response2.getBody();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getHeight()).isEqualTo(350);
    assertThat(image.getWidth()).isEqualTo(350);
  }

  /* 4.4 Quality */
  @Test
  public void testGrayscaling() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("Referer", "http://localhost/foobar");
    ResponseEntity<byte[]> response = restTemplate.exchange(
        "/image/" + IIIFImageApiController.VERSION + "/http-google/0,0,1500,2048/750,/90/gray.jpg",
        HttpMethod.GET, new HttpEntity<>(requestHeaders), byte[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    byte[] imgData = response.getBody();
    BufferedImage image = loadImage(imgData);
    Raster ras = image.getRaster();
    assertThat(ras.getNumDataElements()).isEqualTo(1);
    assertThat(image.getType()).isEqualTo(BufferedImage.TYPE_BYTE_GRAY);
  }

  @Test
  public void testMirror() throws Exception {
    ResponseEntity<byte[]> responseRegular = restTemplate.exchange("/image/" + IIIFImageApiController.VERSION + "/http-google/0,0,1500,2048/750,/0/default.jpg",
                                                                   HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
    assertThat(responseRegular.getStatusCode()).isEqualTo(HttpStatus.OK);
    byte[] imgDataRegular = responseRegular.getBody();

    ResponseEntity<byte[]> responseMirror = restTemplate.exchange("/image/" + IIIFImageApiController.VERSION + "/http-google/0,0,1500,2048/750,/!0/default.jpg",
                                                                  HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
    assertThat(responseMirror.getStatusCode()).isEqualTo(HttpStatus.OK);
    byte[] imgDataMirror = responseMirror.getBody();

    BufferedImage regularImage = loadImage(imgDataRegular);
    BufferedImage mirroredImage = loadImage(imgDataMirror);
    assertThat(mirroredImage.getHeight()).isEqualTo(regularImage.getHeight());
    assertThat(mirroredImage.getWidth()).isEqualTo(regularImage.getWidth());
    assertThat(imgDataRegular).isNotEqualTo(imgDataMirror);
  }

  /* 4.3 Rotation */
  @Test
  public void testRotation() throws Exception {
    ResponseEntity<byte[]> response = restTemplate.exchange("/image/" + IIIFImageApiController.VERSION + "/http-google/0,0,1500,2048/750,/90/default.jpg",
                                                            HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    byte[] imgData = response.getBody();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getHeight()).isEqualTo(750);
    assertThat(image.getWidth()).isEqualTo(1024);
  }

  @Test
  public void testScaleWithBestWidth() throws Exception {
    ResponseEntity<String> response1 = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/file-zoom/full/!500,500/0/default.jpg", String.class);
    assertThat(response1.getStatusCode().series()).isEqualTo(HttpStatus.Series.REDIRECTION);
    String location = response1.getHeaders().getLocation().getPath();
    assertThat(location).isEqualTo("/image/" + IIIFImageApiController.VERSION + "/file-zoom/full/404,500/0/default.jpg");

    ResponseEntity<byte[]> response2 = restTemplate.exchange(location, HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

    byte[] imgData = response2.getBody();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getHeight()).isLessThanOrEqualTo(500);
    assertThat(image.getWidth()).isLessThanOrEqualTo(500);
  }

  @Test
  public void testScaleWithMissingWidth() throws Exception {
    ResponseEntity<String> response1 = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/file-zoom/full/,200/0/default.jpg", String.class);
    assertThat(response1.getStatusCode().series()).isEqualTo(HttpStatus.Series.REDIRECTION);
    String location = response1.getHeaders().getLocation().getPath();
    assertThat(location).isEqualTo("/image/" + IIIFImageApiController.VERSION + "/file-zoom/full/161,200/0/default.jpg");

    ResponseEntity<byte[]> response2 = restTemplate.exchange(location, HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

    byte[] imgData = response2.getBody();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getHeight()).isEqualTo(200);
  }

  /* 4.2 Size */
  @Test
  public void testScaleWithMissingHeigth() throws Exception {
    ResponseEntity<byte[]> response = restTemplate.exchange("/image/" + IIIFImageApiController.VERSION + "/file-zoom/full/200,/0/default.jpg",
                                                            HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    byte[] imgData = response.getBody();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getWidth()).isEqualTo(200);
  }

  @Test
  public void testScaleWithRelativeValues() throws Exception {
    ResponseEntity<String> response1 = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/file-zoom/full/pct:50/0/default.jpg", String.class);
    assertThat(response1.getStatusCode().series()).isEqualTo(HttpStatus.Series.REDIRECTION);
    String location = response1.getHeaders().getLocation().getPath();
    assertThat(location).isEqualTo("/image/" + IIIFImageApiController.VERSION + "/file-zoom/full/1032,/0/default.jpg");

    ResponseEntity<byte[]> response2 = restTemplate.exchange(location, HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

    byte[] imgData = response2.getBody();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getHeight()).isEqualTo(1276);
    assertThat(image.getWidth()).isEqualTo(1032);
  }

  @Test
  public void testRelativeCropWithAbsoluteScale() throws Exception {
    ResponseEntity<String> response1 = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/file-zoom/pct:10,20,20,20/500,/0/default.jpg", String.class);
    assertThat(response1.getStatusCode().series()).isEqualTo(HttpStatus.Series.REDIRECTION);
    String location = response1.getHeaders().getLocation().getPath();
    assertThat(location).isEqualTo("/image/" + IIIFImageApiController.VERSION + "/file-zoom/206,511,413,511/500,/0/default.jpg");

    ResponseEntity<byte[]> response2 = restTemplate.exchange(location, HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

    byte[] imgData = response2.getBody();
    BufferedImage image = loadImage(imgData);
    assertThat(image.getHeight()).isEqualTo(618);
    assertThat(image.getWidth()).isEqualTo(500);
  }

  @Disabled(value = "Fails in travis environment with 400 status code")
  @Test
  public void testUrlEncodedIdentifiers() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("Host", "localhost");
    requestHeaders.add("Referer", "http://localhost/foobar");

    URI addr = new URI("/image/" + IIIFImageApiController.VERSION + "/" + URLEncoder.encode("spec:/ial?file#with[special]ch@arac%ters", "utf8") + "/info.json");
    ResponseEntity<String> response = restTemplate.exchange(addr, HttpMethod.GET, new HttpEntity<>(requestHeaders), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    DocumentContext ctx = JsonPath.parse(response.getBody());
    assertThat(ctx).jsonPathAsString("$.@id")
                   .isEqualTo("http://localhost/image/" + IIIFImageApiController.VERSION + "/spec%3A%2Fial%3Ffile%23with%5Bspecial%5Dch%40arac%25ters");
  }

  @Test
  public void testCanonicalRedirectWithRelativeCropAndScale() throws Exception {
    ResponseEntity<String> response = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/file-zoom/pct:10,20,20,20/pct:84/0/default.jpg", String.class);
    assertThat(response.getStatusCode().series()).isEqualTo(HttpStatus.Series.REDIRECTION);
    String location = response.getHeaders().getLocation().getPath();
    assertThat(location).isEqualTo("/image/" + IIIFImageApiController.VERSION + "/file-zoom/206,511,413,511/346,/0/default.jpg");
  }

  @Test
  public void testNoRedirectIfDisabled() throws Exception {
    iiifController.setCanonicalRedirectEnabled(false);
    ResponseEntity<String> response = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/file-zoom/pct:10,20,20,20/pct:84/0/default.jpg", String.class);
    iiifController.setCanonicalRedirectEnabled(true);
    assertThat(response.getStatusCode().series()).isEqualTo(HttpStatus.Series.SUCCESSFUL);
    String link = response.getHeaders().getFirst("Link").toString();
    assertThat(link).isEqualTo("<http://localhost:" + randomServerPort + "/image/" + IIIFImageApiController.VERSION + "/file-zoom/206,511,413,511/346,/0/default.jpg>;rel=\"canonical\"");
  }

  @Test
  public void testReadPNG() throws Exception {
    ResponseEntity<String> response = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/png-file/full/full/0/default.png", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void testNonExisting() {
    ResponseEntity<String> response = restTemplate.getForEntity("/image/" + IIIFImageApiController.VERSION + "/idontexist/full/full/0/default.png", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getHeaders().getContentType()).isNotEqualTo(MediaType.IMAGE_PNG);
  }

  @Test
  void getImageRepresentationShouldNotAllowPathTraversal() throws UnsupportedEncodingException {
    String url = createUrl("../png-file", "/full/full/0/default.jpg");
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).is(BAD_REQUEST);
    assertThat(response.getBody()).isNullOrEmpty();
  }

  @Test
  void getInfoShouldNotAllowPathTraversal() {
    String url = createUrl("../png-file", "/info.json");
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).is(BAD_REQUEST);
    assertThat(response.getBody()).isNullOrEmpty();
  }

  @Test
  void getInfoRedirectShouldNotAllowPathTraversal() {
    String url = createUrl("../png-file", "");
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.HEAD, HttpEntity.EMPTY, String.class);
    assertThat(response.getStatusCode()).is(BAD_REQUEST);
    assertThat(response.getBody()).isNullOrEmpty();
  }

  private URI createUri(String identifier, String path) {
    String encodedIdentifier = URLEncoder.encode(identifier, StandardCharsets.UTF_8);
    return URI.create("/image/" + IIIFImageApiController.VERSION + "/" + encodedIdentifier + path);
  }

  private String createUrl(String identifier, String path) {
    String encodedIdentifier = URLEncoder.encode(identifier, StandardCharsets.UTF_8);
    return "/image/" + IIIFImageApiController.VERSION + "/" + encodedIdentifier + path;
  }

}
