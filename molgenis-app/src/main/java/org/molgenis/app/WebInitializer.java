package org.molgenis.app;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration.Dynamic;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
  private static final int MB = 1024 * 1024;

  @Override
  protected void customizeRegistration(Dynamic registration) {
    registration.setMultipartConfig(
        new MultipartConfigElement(null, 128L * MB, 128L * MB, 16 * MB));
  }

  @Override
  protected Class<?>[] getRootConfigClasses() {
    return null;
  }

  @Override
  protected Class<?>[] getServletConfigClasses() {
    return new Class[] {WebConfig.class};
  }

  @Override
  protected String[] getServletMappings() {
    return new String[] {"/*"};
  }

  @Override
  protected String getServletName() {
    return "web-" + super.getServletName();
  }
}
