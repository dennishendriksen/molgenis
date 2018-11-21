package org.molgenis.data.annotation;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@SupportedAnnotationTypes("org.molgenis.data.annotation.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class EntityProcessor extends AbstractProcessor {
  private static final String POSTFIX_ENTITY_DEFINITION = "Definition";
  private static final String POSTFIX_ENTITY_METADATA = "Metadata";
  private static final String POSTFIX_ENTITY_FACTORY = "Factory";

  private Messager messager;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    messager = processingEnv.getMessager();
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
                        try {
                          processEntityClass(element);
                        } catch (IOException e) {
                          messager.printMessage(Kind.ERROR, "Error processing @Entity class");
                          throw new UncheckedIOException(e);
                        }
                      } else {
                        messager.printMessage(Kind.ERROR, "@Entity can only be applied to classes");
                      }
                    }));
    return true;
  }

  private void processEntityClass(Element classElement) throws IOException {
    ClassName entityMetadataClassName = createEntityMetadata(classElement);
    ClassName entityClassName = createEntity(classElement, entityMetadataClassName);
    createEntityFactory(classElement, entityMetadataClassName, entityClassName);
  }

  private ClassName createEntityMetadata(Element classElement) {
    String packageName = getPackageName(classElement);
    String className = getEntityName(classElement) + POSTFIX_ENTITY_METADATA;
    return ClassName.get(packageName, className);
  }

  private ClassName createEntity(Element classElement, ClassName entityMetadataClassName) {
    String packageName = getPackageName(classElement);
    String className = getEntityName(classElement);
    return ClassName.get(packageName, className);
  }

  private void createEntityFactory(
      Element classElement, ClassName entityMetadataClassName, ClassName entityClassName)
      throws IOException {
    String packageName = getPackageName(classElement);
    String className = getEntityName(classElement) + POSTFIX_ENTITY_FACTORY;

    ParameterizedTypeName superclassName =
        ParameterizedTypeName.get(
            ClassName.get(AbstractSystemEntityFactory.class),
            entityClassName,
            entityMetadataClassName,
            ClassName.get(String.class));

    ParameterSpec metadataParameterSpec = getParameter(entityMetadataClassName);
    ParameterSpec entityPopulatorParameterSpec = getParameter(ClassName.get(EntityPopulator.class));
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

    TypeSpec helloWorld =
        TypeSpec.classBuilder(className)
            .superclass(superclassName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Component.class)
            .addMethod(constructorSpec)
            .build();

    JavaFile javaFile = JavaFile.builder(packageName, helloWorld).build();
    javaFile.writeTo(processingEnv.getFiler());
  }

  private String getPackageName(Element classElement) {
    PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(classElement);
    return packageElement.getQualifiedName().toString();
  }

  private String getEntityName(Element classElement) {
    String className = classElement.getSimpleName().toString();
    return className.substring(0, className.lastIndexOf(POSTFIX_ENTITY_DEFINITION));
  }

  private static ParameterSpec getParameter(ClassName className) {
    String parameterName = getParameterName(className);
    return ParameterSpec.builder(className, parameterName).build();
  }

  private static String getParameterName(ClassName className) {
    String simpleClassName = className.simpleName();
    return Character.toLowerCase(simpleClassName.charAt(0)) + simpleClassName.substring(1);
  }
}
