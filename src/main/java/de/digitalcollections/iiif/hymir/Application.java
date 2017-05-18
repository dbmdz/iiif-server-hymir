package de.digitalcollections.iiif.hymir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
  public static void main(String[] args) throws Exception {
    if (System.getProperty("spring.profiles.active") == null) {
      System.setProperty("spring.profiles.active", "PROD");
    }
    SpringApplication.run(Application.class, args);
  }
}
