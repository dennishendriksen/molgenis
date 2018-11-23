package org.molgenis.data.annotation.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;

public class GeneratorUtils {
  private GeneratorUtils() {}

  public static ParameterSpec getParameter(ClassName className) {
    String parameterName = getParameterName(className);
    return ParameterSpec.builder(className, parameterName).build();
  }

  private static String getParameterName(ClassName className) {
    String simpleClassName = className.simpleName();
    return Character.toLowerCase(simpleClassName.charAt(0)) + simpleClassName.substring(1);
  }
}
