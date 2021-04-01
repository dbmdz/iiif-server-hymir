package de.digitalcollections.iiif.hymir.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUrlFinder {
  private final HttpClient httpClient;
  private List<UrlSpec> urlSpecs = new ArrayList<>();

  public HttpUrlFinder(List<UrlSpec> urlSpecs) {
    this.urlSpecs = urlSpecs;
    this.httpClient = HttpClient.newBuilder()
        .followRedirects(Redirect.ALWAYS)
        .build();
  }

  public void addSpec(String regex, String template) {
    this.urlSpecs.add(new UrlSpec(regex, template));
  }

  public <T> Optional<T> findAs(String identifier, BodyHandler<T> bodyHandler) throws IOException {
    Optional<URL> url = findAsUrl(identifier);
    if (url.isEmpty()) {
      return Optional.empty();
    }
    try {
      HttpResponse<T> resp = httpClient.send(
          HttpRequest.newBuilder().GET().uri(url.get().toURI()).build(), bodyHandler);
      if (resp.statusCode() >= 400) {
        throw new IOException(
            String.format(
                "Got a bad response code for URL '%s': %d", url.get(), resp.statusCode()));
      }
      return Optional.ofNullable(resp.body());
    } catch (InterruptedException|URISyntaxException e) {
      throw new IOException(e);
    }
  }

  public Optional<InputStream> findAsStream(String identifier) throws IOException {
    return findAs(identifier, BodyHandlers.ofInputStream());
  }

  public Optional<String> findAsString(String identifier) throws IOException {
    return findAs(identifier, BodyHandlers.ofString());
  }

  public Optional<URL> findAsUrl(String identifier) {
    return urlSpecs.stream()
        .map(spec -> spec.urlFor(identifier))
        .filter(Objects::nonNull)
        .findFirst();
  }

  public static class UrlSpec {

    private final Pattern pattern;
    private final String template;

    public UrlSpec(String regex, String template) {
      this.pattern = Pattern.compile(regex);
      this.template = template;
    }

    public URL urlFor(String id) {
      Matcher matcher = pattern.matcher(id);
      if (!matcher.matches()) {
        return  null;
      }
      String[] args = new String[matcher.groupCount()];
      for (int i = 0; i < matcher.groupCount(); i++) {
        args[i] = matcher.group(i + 1);
      }
      try {
        return new URL(String.format(template, (Object[]) args));
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
