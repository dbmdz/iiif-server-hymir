package de.digitalcollections.iiif.hymir.util;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "resourcerepository.resolved")
public class LegacyResolvingProperties {
  private final List<ResolvingSpec> patterns;

  public LegacyResolvingProperties(List<ResolvingSpec> patterns) {
    this.patterns = patterns;
  }

  public List<ResolvingSpec> getUrlSpecs() {
    return patterns.stream()
        .filter(spec -> spec.type == SpecType.URL)
        .collect(Collectors.toList());
  }

  public List<ResolvingSpec> getPathSpecs() {
    return patterns.stream()
        .filter(spec -> spec.type == SpecType.PATH)
        .collect(Collectors.toList());
  }

  public enum SpecType {
    PATH,
    URL,
  }

  public static class ResolvingSpec {
    public final String pattern;
    public final SpecType type;
    public final List<String> substitutions;

    public ResolvingSpec(String pattern, List<String> substitutions) {
      List<SpecType> specTypes = substitutions.stream()
          .map(sub -> {
            if (sub.startsWith("file:/|classpath:/")) {
              return SpecType.PATH;
            } else if (sub.startsWith("http://|https://")) {
              return SpecType.URL;
            }
            return null;
          }).distinct().collect(Collectors.toList());
      if (specTypes.size() != 1) {
        throw new RuntimeException("Resolving patterns with heterogenous protocols are not supported.");
      }
      this.type = specTypes.get(0);
      // TODO: Convert all classpath substitutions to equivalent Path substitutions, throw RTE
      //       if not possible
      this.pattern = pattern;
      if (type == SpecType.PATH) {
        substitutions = substitutions.stream()
            .map(sub -> sub.replace("^file:/", ""))
            .collect(Collectors.toList());
      }
      this.substitutions = substitutions;
    }
  }
}
