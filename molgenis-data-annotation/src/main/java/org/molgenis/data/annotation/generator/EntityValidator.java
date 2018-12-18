package org.molgenis.data.annotation.generator;

import static java.util.Objects.requireNonNull;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

/**
 * TODO add rules and implement all rules
 *
 * <ul>
 *   <li>Attribute names must be unique (note: don't forget to include default names)
 *   <li>Attribute annotations only applied to abstract getters
 *   <li>getter/setter signature must match
 *   <li>Attribute nullable=true: getter returns Optional, setter is annotated with @Nullable
 * </ul>
 */
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
