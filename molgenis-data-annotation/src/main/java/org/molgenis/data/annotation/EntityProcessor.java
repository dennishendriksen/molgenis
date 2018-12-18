package org.molgenis.data.annotation;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import org.molgenis.data.annotation.generator.EntityFactoryGenerator;
import org.molgenis.data.annotation.generator.EntityGenerator;
import org.molgenis.data.annotation.generator.EntityMetadataGenerator;
import org.molgenis.data.annotation.generator.EntityValidator;

@SupportedAnnotationTypes("org.molgenis.data.annotation.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class EntityProcessor extends AbstractProcessor {
  private static final String POSTFIX_ENTITY_METADATA = "Metadata";
  private static final String PREFIX_GENERATED_ENTITY = "Generated_";
  private static final String POSTFIX_ENTITY_FACTORY = "Factory";

  private Messager messager;
  private EntityValidator entityValidator;
  private EntityFactoryGenerator entityFactoryGenerator;
  private EntityMetadataGenerator entityMetadataGenerator;
  private EntityGenerator entityGenerator;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    messager = processingEnv.getMessager();
    entityMetadataGenerator = new EntityMetadataGenerator(processingEnv);
    entityFactoryGenerator = new EntityFactoryGenerator(processingEnv);
    entityGenerator = new EntityGenerator(processingEnv);
    entityValidator = new EntityValidator(processingEnv);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    annotations.forEach(
        annotation ->
            roundEnv
                .getElementsAnnotatedWith(annotation)
                .forEach(
                    element -> {
                      if (element.getKind() == ElementKind.CLASS) {
                        processEntityClass(element);
                      } else {
                        messager.printMessage(Kind.ERROR, "@Entity can only be applied to classes");
                      }
                    }));
    return true;
  }

  private void processEntityClass(Element element) {
    if (entityValidator.isValid(element)) {
      ClassName entityMetadataClassName = createEntityMetadata(element);
      ClassName entityClassName = createEntity(element, entityMetadataClassName);
      createEntityFactory(element, entityMetadataClassName, entityClassName);
    }
  }

  private ClassName createEntityMetadata(Element element) {
    String packageName = getPackageName(element);
    String className = element.getSimpleName() + POSTFIX_ENTITY_METADATA;
    // TODO fix
    //
    //    TypeSpec entityFactoryTypeSpec =
    //        entityMetadataGenerator
    //            .createEntityMetadata(className, entityClassName, entityMetadataClassName)
    //            .addAnnotation(getGeneratedAnnotation())
    //            .build();
    //    writeTypeSpec(packageName, entityFactoryTypeSpec);

    return ClassName.get(packageName, className);
  }

  private ClassName createEntity(Element element, ClassName entityMetadataClassName) {
    String packageName = getPackageName(element);
    String className = PREFIX_GENERATED_ENTITY + element.getSimpleName();

    TypeSpec entityTypeSpec =
        entityGenerator
            .createEntity(element, className, entityMetadataClassName)
            .addAnnotation(getGeneratedAnnotation())
            .build();
    writeTypeSpec(packageName, entityTypeSpec);

    return ClassName.get(packageName, className);
  }

  private void createEntityFactory(
      Element element, ClassName entityMetadataClassName, ClassName entityClassName) {
    // TODO enable once createEntityMetadata works
    //    String packageName = getPackageName(element);
    //    String className = element.getSimpleName() + POSTFIX_ENTITY_FACTORY;
    //
    //    TypeSpec entityFactoryTypeSpec =
    //        entityFactoryGenerator
    //            .createEntityFactory(className, entityClassName, entityMetadataClassName)
    //            .addAnnotation(getGeneratedAnnotation())
    //            .build();
    //    writeTypeSpec(packageName, entityFactoryTypeSpec);
  }

  private String getPackageName(Element element) {
    PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(element);
    return packageElement.getQualifiedName().toString();
  }

  private void writeTypeSpec(String packageName, TypeSpec typeSpec) {
    JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
    try {
      javaFile.writeTo(processingEnv.getFiler());
    } catch (IOException e) {
      messager.printMessage(Kind.ERROR, "Error processing @Entity class");
      throw new UncheckedIOException(e);
    }
  }

  private AnnotationSpec getGeneratedAnnotation() {
    return AnnotationSpec.builder(Generated.class)
        .addMember("value", '"' + this.getClass().getName() + '"')
        .build();
  }
}
