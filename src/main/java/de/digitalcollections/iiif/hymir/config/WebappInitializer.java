package de.digitalcollections.iiif.hymir.config;

import com.twelvemonkeys.servlet.image.IIOProviderContextListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Replaces web.xml.
 */
public class WebappInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebappInitializer.class);

  @Override
  protected Class<?>[] getRootConfigClasses() {
    return new Class<?>[]{SpringConfig.class};
  }

  @Override
  protected Class<?>[] getServletConfigClasses() {
    return null;
  }

  @Override
  protected String[] getServletMappings() {
    return new String[]{"/*"};
  }

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    super.onStartup(servletContext);
    servletContext.addListener(new IIOProviderContextListener());

    String contextPath = servletContext.getContextPath();
    LOGGER.info("*** Deployed under context path '{}'", contextPath);
  }
}
