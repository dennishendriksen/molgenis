package org.molgenis.data.annotation.generator;

import static java.util.Objects.requireNonNull;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

public class EntityValidator {
  private final ProcessingEnvironment processingEnv;
  private final Messager messager;

  public EntityValidator(ProcessingEnvironment processingEnv) {
    this.processingEnv = requireNonNull(processingEnv);
    messager = processingEnv.getMessager();
  }

  public boolean isValid(Element element) {
    return true;
  }
}
