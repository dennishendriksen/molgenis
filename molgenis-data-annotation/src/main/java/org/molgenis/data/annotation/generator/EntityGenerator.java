package org.molgenis.data.annotation.generator;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import java.beans.Introspector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic.Kind;
import org.molgenis.data.annotation.Attribute;

/**
 * TODO: add validation rules
 *
 * <ul>
 *   <li>Attribute names must be unique (note: don't forget to include default names)
 *   <li>Attribute annotations only applied to abstract getters
 *   <li>getter/setter signature must match
 *   <li>Attribute nullable=true: getter returns Optional, setter is annotated with @Nullable
 * </ul>
 */
public class EntityGenerator {
  private final ProcessingEnvironment processingEnv;
  private final Messager messager;

  public EntityGenerator(ProcessingEnvironment processingEnv) {
    this.processingEnv = requireNonNull(processingEnv);
    messager = processingEnv.getMessager();
  }

  public Builder createEntity(
      Element element, String className, ClassName entityMetadataClassName) {
    TypeName superclassName = TypeName.get(element.asType());

    Map<String, Attribute> baseMethodAttributeMap = createBaseMethodAttributeMap(element);

    // create fields and methods
    List<FieldSpec> fieldSpecs = new ArrayList<>();
    List<MethodSpec> methodSpecs = new ArrayList<>();

    element
        .getEnclosedElements()
        .forEach(
            enclosedElement -> {
              if (isAttributeElement(enclosedElement)) {
                handleAttributeElement(
                    (ExecutableElement) enclosedElement,
                    fieldSpecs,
                    methodSpecs,
                    baseMethodAttributeMap);
              } else if (isAbstractElement(enclosedElement)) {
                handleAbstractAttributeGetter(
                    (ExecutableElement) enclosedElement, methodSpecs, baseMethodAttributeMap);
              }
            });

    return TypeSpec.classBuilder(className)
        .superclass(superclassName)
        .addModifiers(Modifier.FINAL)
        .addFields(fieldSpecs)
        .addMethods(methodSpecs);
  }

  private Map<String, Attribute> createBaseMethodAttributeMap(Element element) {
    Map<String, Attribute> attributeNameMethodMap = new HashMap<>();
    element
        .getEnclosedElements()
        .forEach(
            enclosedElement -> {
              Attribute attributeAnnotation = enclosedElement.getAnnotation(Attribute.class);
              if (attributeAnnotation != null) {
                String baseMethodName = getBaseMethodName(enclosedElement);
                attributeNameMethodMap.put(baseMethodName, attributeAnnotation);
              }
            });
    return attributeNameMethodMap;
  }

  private String getBaseMethodName(Element enclosedElement) {
    String methodName = enclosedElement.getSimpleName().toString();
    String baseMethodName;
    if (methodName.startsWith("get")) {
      baseMethodName = methodName.substring("get".length());
    } else if (methodName.startsWith("set")) {
      baseMethodName = methodName.substring("set".length());
    } else if (methodName.startsWith("is")) {
      baseMethodName = methodName.substring("is".length());
    } else {
      messager.printMessage(Kind.ERROR, "Illegal method name", enclosedElement);
      throw new RuntimeException();
    }
    return baseMethodName;
  }

  private String getFieldName(Attribute attributeAnnotation, Element attributeElement) {
    String attributeName = attributeAnnotation.name();
    String fieldName;
    if (!attributeName.isEmpty()) {
      fieldName = attributeName;
    } else {
      fieldName = Introspector.decapitalize(getBaseMethodName(attributeElement));
    }
    return fieldName;
  }

  private void handleAttributeElement(
      ExecutableElement attributeElement,
      List<FieldSpec> fieldSpecs,
      List<MethodSpec> methodSpecs,
      Map<String, Attribute> baseMethodAttributeMap) {

    if (!attributeElement.getModifiers().contains(Modifier.ABSTRACT)) {
      messager.printMessage(
          Kind.ERROR, "@Attribute can only be applied to abstract methods", attributeElement);
    }

    Attribute attributeAnnotation = attributeElement.getAnnotation(Attribute.class);
    boolean nullable = attributeAnnotation.nullable();

    // write attribute field
    TypeName returnTypeName = TypeName.get(attributeElement.getReturnType());
    TypeName fieldTypeName;
    if (nullable && !returnTypeName.equals(ClassName.get(Iterable.class))) {
      fieldTypeName =
          ((ParameterizedTypeName) TypeName.get(attributeElement.getReturnType()))
              .typeArguments.get(0);
    } else {
      fieldTypeName = returnTypeName;
    }
    String fieldName = getFieldName(attributeAnnotation, attributeElement);

    FieldSpec fieldSpec = FieldSpec.builder(fieldTypeName, fieldName, Modifier.PRIVATE).build();
    fieldSpecs.add(fieldSpec);

    // write attribute method
    String methodName = attributeElement.getSimpleName().toString();
    Iterable<Modifier> methodModifiers =
        attributeElement
            .getModifiers()
            .stream()
            .filter(modifier -> modifier != Modifier.ABSTRACT)
            .collect(toList());
    MethodSpec.Builder methodSpecBuilder =
        MethodSpec.methodBuilder(methodName)
            .addAnnotation(Override.class)
            .addModifiers(methodModifiers)
            .returns(returnTypeName);
    if (nullable && !returnTypeName.equals(ClassName.get(Iterable.class))) {
      methodSpecBuilder.addStatement("return $T.ofNullable($N)", Optional.class, fieldName);
    } else {
      methodSpecBuilder.addStatement("return $N", fieldName);
    }

    MethodSpec methodSpec = methodSpecBuilder.build();
    methodSpecs.add(methodSpec);
  }

  private void handleAbstractAttributeGetter(
      ExecutableElement abstractElement,
      List<MethodSpec> methodSpecs,
      Map<String, Attribute> attributeNameMethodMap) {
    String methodName = abstractElement.getSimpleName().toString();
    Attribute attribute = attributeNameMethodMap.get(getBaseMethodName(abstractElement));
    String fieldName = getFieldName(attribute, abstractElement);
    Iterable<Modifier> methodModifiers =
        abstractElement
            .getModifiers()
            .stream()
            .filter(modifier -> modifier != Modifier.ABSTRACT)
            .collect(toList());
    List<? extends VariableElement> parameters = abstractElement.getParameters();
    if (parameters.size() != 1) {
      messager.printMessage(Kind.ERROR, "Method must have one parameter", abstractElement);
    }
    VariableElement parameterElement = parameters.get(0);
    String parameterName = parameterElement.getSimpleName().toString();

    ParameterSpec.Builder parameterSpecBuilder =
        ParameterSpec.builder(TypeName.get(parameterElement.asType()), parameterName);
    if (attribute.nullable()) {
      parameterSpecBuilder.addAnnotation(Nullable.class);
    }
    ParameterSpec parameterSpec = parameterSpecBuilder.build();
    MethodSpec methodSpec =
        MethodSpec.methodBuilder(methodName)
            .addAnnotation(Override.class)
            .addModifiers(methodModifiers)
            .returns(TypeName.get(abstractElement.getReturnType()))
            .addParameter(parameterSpec)
            .addStatement("this.$N = $N", fieldName, parameterName)
            .addStatement("return this")
            .build();

    methodSpecs.add(methodSpec);
  }

  private boolean isAbstractElement(Element element) {
    return element.getModifiers().contains(Modifier.ABSTRACT);
  }

  private boolean isAttributeElement(Element element) {
    return element.getAnnotation(Attribute.class) != null;
  }
}
