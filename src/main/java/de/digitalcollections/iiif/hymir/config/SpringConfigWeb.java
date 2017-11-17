package de.digitalcollections.iiif.hymir.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.yaml.snakeyaml.Yaml;

@Configuration(value = "SpringConfigWebHymir")
public class SpringConfigWeb extends WebMvcConfigurerAdapter {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
    localeChangeInterceptor.setParamName("language");
    registry.addInterceptor(localeChangeInterceptor);
  }

  @Bean(name = "localeResolver")
  public LocaleResolver sessionLocaleResolver() {
    SessionLocaleResolver localeResolver = new SessionLocaleResolver();
    localeResolver.setDefaultLocale(Locale.GERMAN);
    return localeResolver;
  }

  @Bean(name = "iiifVersions")
  public Map<String, String> iiifVersions() {
    Map<String, String> versions = null;
    Map<String, String> customVersions = null;
    Yaml yaml = new Yaml();
    try (InputStream in = this.getClass().getResourceAsStream("/iiif-versions.yml")) {
      versions = (Map<String, String>) yaml.load(in);
    } catch (IOException exception) {
      throw new IllegalStateException(exception);
    }
    try (InputStream in = this.getClass().getResourceAsStream("/iiif-versions-custom.yml")) {
      customVersions = (Map<String, String>) yaml.load(in);
      if (customVersions != null) {
        customVersions.forEach(versions::putIfAbsent);
      }
    } catch (IOException exception) {
      throw new IllegalStateException(exception);
    }
    return versions;
  }
}
