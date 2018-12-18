package org.molgenis.data.annotation.generator;

import static java.util.Objects.requireNonNull;

import javax.annotation.processing.ProcessingEnvironment;

public class EntityMetadataGenerator {

  private final ProcessingEnvironment processingEnv;

  public EntityMetadataGenerator(ProcessingEnvironment processingEnv) {
    this.processingEnv = requireNonNull(processingEnv);
  }
}
