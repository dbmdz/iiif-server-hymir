package de.digitalcollections.iiif.hymir.config;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "custom.iiif.headers")
@ConstructorBinding
public class CustomResponseHeaders {

  private final List<ResponseHeader> imageTile;
  private final List<ResponseHeader> imageInfo;
  private final List<ResponseHeader> presentationManifest;
  private final List<ResponseHeader> presentationAnnotationList;
  private final List<ResponseHeader> presentationCollection;

  // custom.iiif.headers
  public CustomResponseHeaders(
      List<ResponseHeader> all,
      ImageResponseHeaders image,
      PresentationResponseHeaders presentation) {
    all = Objects.requireNonNullElseGet(all, Collections::emptyList);
    image = Objects.requireNonNullElseGet(image, ImageResponseHeaders::empty);
    presentation = Objects.requireNonNullElseGet(presentation, PresentationResponseHeaders::empty);
    this.imageTile = concatenate(all, image.image);
    this.imageInfo = concatenate(all, image.info);
    this.presentationManifest = concatenate(all, presentation.manifest);
    this.presentationAnnotationList = concatenate(all, presentation.annotationList);
    this.presentationCollection = concatenate(all, presentation.collection);
  }

  private List<ResponseHeader> concatenate(
      List<ResponseHeader> first, List<ResponseHeader> second) {
    List<ResponseHeader> result = new ArrayList<>(first);
    result.addAll(second);
    return result;
  }

  public List<ResponseHeader> forImageTile() {
    return unmodifiableList(imageTile);
  }

  public List<ResponseHeader> forImageInfo() {
    return unmodifiableList(imageInfo);
  }

  public List<ResponseHeader> forPresentationManifest() {
    return unmodifiableList(presentationManifest);
  }

  public List<ResponseHeader> forPresentationCollection() {
    return unmodifiableList(presentationCollection);
  }

  public List<ResponseHeader> forPresentationAnnotationList() {
    return unmodifiableList(presentationAnnotationList);
  }

  protected static class ImageResponseHeaders {

    private final List<ResponseHeader> image;
    private final List<ResponseHeader> info;

    public ImageResponseHeaders(List<ResponseHeader> image, List<ResponseHeader> info) {
      this.image = unmodifiableList(Objects.requireNonNullElseGet(image, Collections::emptyList));
      this.info = unmodifiableList(Objects.requireNonNullElseGet(info, Collections::emptyList));
    }

    public List<ResponseHeader> getImage() {
      return unmodifiableList(image);
    }

    public List<ResponseHeader> getInfo() {
      return unmodifiableList(info);
    }

    public static ImageResponseHeaders empty() {
      return new ImageResponseHeaders(emptyList(), emptyList());
    }
  }

  @ConstructorBinding
  protected static class PresentationResponseHeaders {

    private final List<ResponseHeader> manifest;
    private final List<ResponseHeader> collection;
    private final List<ResponseHeader> annotationList;

    public PresentationResponseHeaders(
        List<ResponseHeader> manifest,
        List<ResponseHeader> collection,
        List<ResponseHeader> annotationList) {
      this.manifest = Objects.requireNonNullElseGet(manifest, Collections::emptyList);
      this.collection = Objects.requireNonNullElseGet(collection, Collections::emptyList);
      this.annotationList = Objects.requireNonNullElseGet(annotationList, Collections::emptyList);
    }

    public List<ResponseHeader> getManifest() {
      return unmodifiableList(manifest);
    }

    public List<ResponseHeader> getCollection() {
      return unmodifiableList(collection);
    }

    public List<ResponseHeader> getAnnotationList() {
      return unmodifiableList(annotationList);
    }

    public static PresentationResponseHeaders empty() {
      return new PresentationResponseHeaders(emptyList(), emptyList(), emptyList());
    }
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
