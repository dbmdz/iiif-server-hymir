package de.digitalcollections.iiif.hymir;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

@SpringBootApplication
public class Application implements InitializingBean {

  @Value("${rules}")
  private String rulesPath;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (!StringUtils.isEmpty(rulesPath)) {
      System.setProperty("multiPatternResolvingFile", rulesPath);
    }
  }
}
