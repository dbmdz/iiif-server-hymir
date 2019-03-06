package de.digitalcollections.iiif.hymir.config;

import de.digitalcollections.commons.springmvc.interceptors.CurrentUrlAsModelAttributeHandlerInterceptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.util.UrlPathHelper;
import org.yaml.snakeyaml.Yaml;

@Configuration
public class SpringConfigWeb implements WebMvcConfigurer {

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
    localeChangeInterceptor.setParamName("language");
    registry.addInterceptor(localeChangeInterceptor);

    CurrentUrlAsModelAttributeHandlerInterceptor currentUrlAsModelAttributeHandlerInterceptor = new CurrentUrlAsModelAttributeHandlerInterceptor();
    currentUrlAsModelAttributeHandlerInterceptor.deleteParams("language");
    registry.addInterceptor(currentUrlAsModelAttributeHandlerInterceptor);
  }

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    // Needed for escaped slashes in identifiers
    UrlPathHelper urlPathHelper = new UrlPathHelper();
    urlPathHelper.setUrlDecode(false);
    configurer.setUrlPathHelper(urlPathHelper);
  }

  @Bean
  public LocaleResolver localeResolver() {
    SessionLocaleResolver localeResolver = new SessionLocaleResolver();
    localeResolver.setDefaultLocale(Locale.ENGLISH);
    return localeResolver;
  }

  @Bean
  public Map<String, String> iiifVersions() {
    Map<String, String> versions;
    Map<String, String> customVersions;
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
