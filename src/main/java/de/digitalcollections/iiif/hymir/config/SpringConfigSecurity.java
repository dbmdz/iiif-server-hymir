package de.digitalcollections.iiif.hymir.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

@Configuration
public class SpringConfigSecurity extends WebSecurityConfigurerAdapter {

  @Value("${spring.security.user.name}")
  private String actuatorUsername;

  @Value("${spring.security.user.password}")
  private String actuatorPassword;

  @Bean
  public HttpFirewall looseFirewall() {
    DefaultHttpFirewall firewall = new DefaultHttpFirewall();
    firewall.setAllowUrlEncodedSlash(true);
    return firewall;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication().passwordEncoder(passwordEncoderDummy())
        .withUser(User.withUsername(actuatorUsername).password(actuatorPassword).roles("ACTUATOR"));
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // Monitoring:
    // see https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready-endpoints
    http.antMatcher("/monitoring/**").authorizeRequests()
        .requestMatchers(EndpointRequest.to(InfoEndpoint.class, HealthEndpoint.class)).permitAll()
        .requestMatchers(EndpointRequest.to("jolokia", "prometheus", "version")).permitAll()
        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR").and().httpBasic();
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    // We need to loosen the firewall settings to allow various urlencoded characters in identifiers
    web.httpFirewall(looseFirewall());
  }

  private PasswordEncoder passwordEncoderDummy() {
    return new PasswordEncoder() {
      @Override
      public String encode(CharSequence rawPassword) {
        return rawPassword.toString();
      }

      @Override
      public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return rawPassword.toString().equals(encodedPassword);
      }
    };
  }

}
