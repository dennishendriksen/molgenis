package org.molgenis.app;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class ApiInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
  @Override
  protected Class<?>[] getRootConfigClasses() {
    return null;
  }

  @Override
  protected Class<?>[] getServletConfigClasses() {
    return new Class[] {ApiConfig.class};
  }

  @Override
  protected String[] getServletMappings() {
    return new String[] {"/api/*"};
  }

  @Override
  protected String getServletName() {
    return "api-" + super.getServletName();
  }
}
