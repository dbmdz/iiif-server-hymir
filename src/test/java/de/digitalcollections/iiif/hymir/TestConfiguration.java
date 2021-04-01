package de.digitalcollections.iiif.hymir;

import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import de.digitalcollections.iiif.hymir.image.business.api.ImageSecurityService;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import java.net.URI;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TestConfiguration implements WebMvcConfigurer {

  @Bean
  public IiifObjectMapper iiifObjectMapper() {
    return new IiifObjectMapper();
  }

  @Bean
  public ImageSecurityService imageSecurityService() {
    return new ImageSecurityService() {
      @Override
      public boolean isAccessAllowed(String identifier) {
        return true;
      }

      @Override
      public URI getLicense(String identifier) {
        return URI.create("https://example.com/my-license");
      }
    };
  }

  public static void setDefaults() {
    com.jayway.jsonpath.Configuration.setDefaults(
        new com.jayway.jsonpath.Configuration.Defaults() {
          private final JsonProvider jsonProvider = new JacksonJsonProvider();
          private final MappingProvider mappingProvider = new JacksonMappingProvider();

          @Override
          public JsonProvider jsonProvider() {
            return jsonProvider;
          }

          @Override
          public MappingProvider mappingProvider() {
            return mappingProvider;
          }

          @Override
          public Set<Option> options() {
            return EnumSet.noneOf(Option.class);
          }
        });
  }
}
