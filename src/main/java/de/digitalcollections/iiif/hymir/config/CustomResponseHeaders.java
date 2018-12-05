package de.digitalcollections.iiif.hymir.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "custom.iiif.headers")
public class CustomResponseHeaders {

  private List<ResponseHeader> all = new ArrayList<>();
  private HashMap<String, List<ResponseHeader>> image = new HashMap<>();
  private HashMap<String, List<ResponseHeader>> presentation = new HashMap<>();

  // custom.iiif.headers.all
  public void setAll(List<ResponseHeader> all) {
    this.all = all;
  }

  public List<ResponseHeader> getAll() {
    return this.all;
  }

  // custom.iiif.headers.image
  public HashMap<String, List<ResponseHeader>> getImage() {
    return image;
  }

  public void setImage(HashMap<String, List<ResponseHeader>> image) {
    this.image = image;
  }

  // custom.iiif.headers.presentation
  public void setPresentation(HashMap<String, List<ResponseHeader>> presentation) {
    this.presentation = presentation;
  }

  public HashMap<String, List<ResponseHeader>> getPresentation() {
    return presentation;
  }

  public List<ResponseHeader> forImageTile() {
    List<ResponseHeader> result = new ArrayList<>();
    Optional.ofNullable(all).ifPresent(result::addAll);
    Optional.ofNullable(image.get("image")).ifPresent(result::addAll);
    return result;
  }

  public List<ResponseHeader> forImageInfo() {
    List<ResponseHeader> result = new ArrayList<>();
    Optional.ofNullable(all).ifPresent(result::addAll);
    Optional.ofNullable(image.get("info")).ifPresent(result::addAll);
    return result;
  }

  public List<ResponseHeader> forPresentationManifest() {
    List<ResponseHeader> result = new ArrayList<>();
    Optional.ofNullable(all).ifPresent(result::addAll);
    Optional.ofNullable(presentation.get("manifest")).ifPresent(result::addAll);
    return result;
  }

  public List<ResponseHeader> forPresentationCollection() {
    List<ResponseHeader> result = new ArrayList<>();
    Optional.ofNullable(all).ifPresent(result::addAll);
    Optional.ofNullable(presentation.get("collection")).ifPresent(result::addAll);
    return result;
  }

  public static class ResponseHeader {

    private String name;
    private String value;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

  }
}
