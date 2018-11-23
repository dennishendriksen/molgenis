package org.molgenis.data.annotation.generator;

import static java.util.Objects.requireNonNull;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

public class EntityFactoryGenerator {
  private final ProcessingEnvironment processingEnv;
  private final Messager messager;

  public EntityFactoryGenerator(ProcessingEnvironment processingEnv) {
    this.processingEnv = requireNonNull(processingEnv);
    messager = processingEnv.getMessager();
  }

  public Builder createEntityFactory(
      String className, ClassName entityClassName, ClassName entityMetadataClassName) {
    ParameterizedTypeName superclassName =
        ParameterizedTypeName.get(
            ClassName.get(AbstractSystemEntityFactory.class),
            entityClassName,
            entityMetadataClassName,
            ClassName.get(String.class));

    ParameterSpec metadataParameterSpec = GeneratorUtils.getParameter(entityMetadataClassName);
    ParameterSpec entityPopulatorParameterSpec =
        GeneratorUtils.getParameter(ClassName.get(EntityPopulator.class));
    MethodSpec constructorSpec =
        MethodSpec.constructorBuilder()
            .addParameter(metadataParameterSpec)
            .addParameter(entityPopulatorParameterSpec)
            .addStatement(
                "super($T.class, $N, $N);",
                entityClassName,
                metadataParameterSpec,
                entityPopulatorParameterSpec)
            .build();

    return TypeSpec.classBuilder(className)
        .superclass(superclassName)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(Component.class)
        .addMethod(constructorSpec);
  }
}
