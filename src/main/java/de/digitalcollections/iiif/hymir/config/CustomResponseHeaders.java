package de.digitalcollections.iiif.hymir.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "custom.iiif.headers")
@ConstructorBinding
// False positive: All response header lists <em>are immutable</em>, but Spotbugs does not recognize
// this.
@SuppressFBWarnings("EI_EXPOSE_REP")
public class CustomResponseHeaders {

  private final List<ResponseHeader> imageTile;
  private final List<ResponseHeader> imageInfo;
  private final List<ResponseHeader> presentationManifest;
  private final List<ResponseHeader> presentationCollection;
  private final List<ResponseHeader> presentationAnnotation;

  /**
   * @param all custom.iiif.headers.all
   * @param image custom.iiif.headers.image
   * @param presentation custom.iiif.headers.presentation
   */
  public CustomResponseHeaders(
      List<ResponseHeader> all,
      HashMap<String, List<ResponseHeader>> image,
      HashMap<String, List<ResponseHeader>> presentation) {

    // just make sure there is a top level instance for each to make the following code easy
    all = Objects.requireNonNullElseGet(all, ArrayList::new);
    image = Objects.requireNonNullElseGet(image, HashMap::new);
    presentation = Objects.requireNonNullElseGet(presentation, HashMap::new);

    this.imageTile = combineHeaders(all, image.get("image"));
    this.imageInfo = combineHeaders(all, image.get("info"));
    this.presentationManifest = combineHeaders(all, presentation.get("manifest"));
    this.presentationCollection = combineHeaders(all, presentation.get("collection"));
    this.presentationAnnotation = combineHeaders(all, presentation.get("annotationList"));
  }

  /**
   * Concatenates two nullable lists of response headers into one immutable list.
   *
   * @param headers1 The first list of headers (can be null)
   * @param headers2 The second list of headers (can be null)
   * @return A immutable list containing all headers
   */
  private List<ResponseHeader> combineHeaders(
      List<ResponseHeader> headers1, List<ResponseHeader> headers2) {
    List<ResponseHeader> result = new ArrayList<>();
    if (headers1 != null) {
      result.addAll(headers1);
    }
    if (headers2 != null) {
      result.addAll(headers2);
    }
    return List.copyOf(result);
  }

  public List<ResponseHeader> forImageTile() {
    return List.copyOf(imageTile);
  }

  public List<ResponseHeader> forImageInfo() {
    return imageInfo;
  }

  public List<ResponseHeader> forPresentationManifest() {
    return presentationManifest;
  }

  public List<ResponseHeader> forPresentationCollection() {
    return presentationCollection;
  }

  public List<ResponseHeader> forPresentationAnnotationList() {
    return presentationAnnotation;
  }

  public static class ResponseHeader {

    private final String name;
    private final String value;

    public ResponseHeader(String name, String value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public String getValue() {
      return value;
    }
  }
}
