package org.molgenis.data.annotation.generator;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import com.squareup.javapoet.TypeVariableName;
import java.beans.Introspector;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.validation.constraints.NotEmpty;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.Attribute;
import org.molgenis.data.meta.model.EntityType;

// TODO set empty list by default for mrefs
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
                handleAbstractAttributeSetter(
                    (ExecutableElement) enclosedElement, methodSpecs, baseMethodAttributeMap);
              }
            });
    AttributeSpec attributeSpec =
        new AttributeSpec("id", FieldSpec.builder(String.class, "id").build(), true, true);
    Map<String, AttributeSpec> attributeSpecsMap = singletonMap("id", attributeSpec);
    AttributeSpecs attributeSpecs = new AttributeSpecs(attributeSpecsMap);

    methodSpecs.add(createEntityType());
    methodSpecs.add(createGetAttributeNames(attributeSpecs));
    methodSpecs.addAll(createGetSetIdValue(attributeSpecs));
    methodSpecs.add(createGetLabelValue(attributeSpecs));
    methodSpecs.add(createGet(attributeSpecs));
    methodSpecs.addAll(createGenericGetters());
    methodSpecs.addAll(createSetters(attributeSpecs));
    return TypeSpec.classBuilder(className)
        .superclass(superclassName)
        .addModifiers(Modifier.FINAL)
        .addFields(fieldSpecs)
        .addMethods(methodSpecs);
  }

  private MethodSpec createEntityType() {
    return methodBuilder("getEntityType")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(EntityType.class)
        .addStatement("throw new $T(\"TODO implement\")", RuntimeException.class)
        .build();
  }

  private MethodSpec createGetAttributeNames(AttributeSpecs attributeSpecs) {
    ParameterizedTypeName iterableType = ParameterizedTypeName.get(Iterable.class, String.class);
    String literal =
        attributeSpecs
            .get()
            .stream()
            .map(attributeSpec -> '"' + attributeSpec.getAttributeName() + '"')
            .collect(joining(", "));
    return methodBuilder("getAttributeNames")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(iterableType)
        .addStatement("return $T.asList($L)", Arrays.class, literal)
        .build();
  }

  private List<MethodSpec> createGetSetIdValue(AttributeSpecs attributeSpecs) {
    List<MethodSpec> methodSpecs = new ArrayList<>();

    Optional<AttributeSpec> idAttributeSpecOptional = attributeSpecs.getIdAttributeSpec();
    if (idAttributeSpecOptional.isPresent()) {
      AttributeSpec idAttributeSpec = idAttributeSpecOptional.get();
      String attributeName = idAttributeSpec.getAttributeName();
      FieldSpec field = idAttributeSpec.getField();

      methodSpecs.add(
          methodBuilder("getIdValue")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(Object.class)
              .addStatement("return $N", attributeName)
              .build());

      ParameterSpec idParameter = ParameterSpec.builder(Object.class, "id").build();

      methodSpecs.add(
          methodBuilder("setIdValue")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(idParameter)
              .addStatement("this.$N = ($T) $N", attributeName, field.type, idParameter)
              .build());

    } else {
      throw new RuntimeException("TODO implement (call super)");
    }
    return methodSpecs;
  }

  private MethodSpec createGetLabelValue(AttributeSpecs attributeSpecs) {
    MethodSpec methodSpec;

    Optional<AttributeSpec> labelAttributeSpecOptional = attributeSpecs.getLabelAttributeSpec();
    if (labelAttributeSpecOptional.isPresent()) {
      AttributeSpec labelAttributeSpec = labelAttributeSpecOptional.get();

      methodSpec =
          methodBuilder("getLabelValue")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(Object.class)
              .addStatement("return $N", labelAttributeSpec.getAttributeName())
              .build();
    } else {
      throw new RuntimeException("TODO implement (call super)");
    }
    return methodSpec;
  }

  /** Returns an implementation for {@link Entity#set(String, Object)}. */
  private List<MethodSpec> createSetters(AttributeSpecs attributeSpecs) {
    List<MethodSpec> methodSpecs = new ArrayList<>();

    methodSpecs.add(createSetAttributeValue(attributeSpecs));
    methodSpecs.add(createSetEntity(attributeSpecs));

    return methodSpecs;
  }

  private MethodSpec createSetEntity(AttributeSpecs attributeSpecs) {
    ParameterSpec entityParameter = ParameterSpec.builder(Entity.class, "entity").build();

    MethodSpec.Builder methodBuilder =
        methodBuilder("set")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(entityParameter)
            .addStatement("throw new $T(\"TODO implement\")", RuntimeException.class);

    // TODO add implementation
    return methodBuilder.build();
  }

  private MethodSpec createSetAttributeValue(AttributeSpecs attributeSpecs) {
    // set(String attributeName, String value)
    ParameterSpec attributeNameParameter = createAttributeNameParameter();
    ParameterSpec valueParameter = ParameterSpec.builder(Object.class, "value").build();
    MethodSpec.Builder methodBuilder =
        methodBuilder("set")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(attributeNameParameter)
            .addParameter(valueParameter);

    methodBuilder.beginControlFlow("switch($N)", attributeNameParameter);
    attributeSpecs
        .get()
        .forEach(
            attributeSpec -> {
              FieldSpec field = attributeSpec.getField();
              methodBuilder.addCode("case $S:\n", attributeSpec.getAttributeName());
              // TODO validate value type
              // TODO validate not-null for mrefs
              methodBuilder.addStatement("  this.$N = ($T) $N", field, field.type, valueParameter);
              methodBuilder.addStatement("  break");
            });
    methodBuilder.addCode("default:\n");
    methodBuilder.addStatement(
        "  throw new $T($N)", IllegalArgumentException.class, attributeNameParameter);
    methodBuilder.endControlFlow();

    methodBuilder.addStatement("return");
    return methodBuilder.build();
  }

  /** Returns an implementation for {@link Entity#get(String)}. */
  private MethodSpec createGet(AttributeSpecs attributeSpecs) {
    ParameterSpec attributeNameParameter = createAttributeNameParameter();
    MethodSpec.Builder methodBuilder =
        methodBuilder("get")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(Object.class)
            .addParameter(attributeNameParameter);

    methodBuilder.beginControlFlow("switch($N)", attributeNameParameter);
    attributeSpecs
        .get()
        .forEach(
            attributeSpec -> {
              methodBuilder.addCode("case $S:\n", attributeSpec.getAttributeName());
              methodBuilder.addStatement("  return $N", attributeSpec.getField());
            });
    methodBuilder.addCode("default:\n");
    methodBuilder.addStatement(
        "  throw new $T($N)", IllegalArgumentException.class, attributeNameParameter);
    methodBuilder.endControlFlow();

    return methodBuilder.build();
  }

  /** Returns implementations for {@link Entity#getBoolean(String)} and similar methods. */
  private List<MethodSpec> createGenericGetters() {
    List<MethodSpec> methodSpecs = new ArrayList<>();

    ParameterizedTypeName iterableType = ParameterizedTypeName.get(Iterable.class, Entity.class);

    methodSpecs.add(createGetter("getBoolean", Boolean.class));
    methodSpecs.add(createGetter("getDouble", Double.class));
    methodSpecs.add(createGetter("getEntity", Entity.class));
    methodSpecs.add(createGetter("getEntities", iterableType));
    methodSpecs.add(createGetter("getInstant", Instant.class));
    methodSpecs.add(createGetter("getInt", Integer.class));
    methodSpecs.add(createGetter("getLocalDate", LocalDate.class));
    methodSpecs.add(createGetter("getLong", Long.class));
    methodSpecs.add(createGetter("getString", String.class));

    TypeVariableName entityType = TypeVariableName.get("E", Entity.class);
    ParameterizedTypeName typedIterableType =
        ParameterizedTypeName.get(ClassName.get(Iterable.class), entityType);

    methodSpecs.add(createTypedGetter("getEntity", entityType, entityType));
    methodSpecs.add(createTypedGetter("getEntities", typedIterableType, entityType));

    return methodSpecs;
  }

  private MethodSpec createGetter(String methodName, Class<?> returnClass) {
    ClassName returnType = ClassName.get(returnClass);
    return createGetter(methodName, returnType);
  }

  private MethodSpec createGetter(String methodName, TypeName returnType) {
    ParameterSpec attributeNameParam = createAttributeNameParameter();
    return MethodSpec.methodBuilder(methodName)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(returnType)
        .addParameter(attributeNameParam)
        .addStatement("return ($T) get($N)", returnType, attributeNameParam)
        .build();
  }

  private MethodSpec createTypedGetter(
      String methodName, TypeName returnType, TypeVariableName entityType) {
    ParameterSpec attributeNameParam = createAttributeNameParameter();

    ParameterizedTypeName classType =
        ParameterizedTypeName.get(ClassName.get(Class.class), entityType);
    return methodBuilder(methodName)
        .addAnnotation(
            AnnotationSpec.builder(SuppressWarnings.class)
                .addMember("value", "\"unchecked\"")
                .build())
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addTypeVariable(entityType)
        .returns(returnType)
        .addParameter(attributeNameParam)
        .addParameter(classType, "clazz")
        .addStatement("return ($T) get($N)", returnType, attributeNameParam)
        .build();
  }

  private ParameterSpec createAttributeNameParameter() {
    return ParameterSpec.builder(String.class, "attributeName").build();
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
    if (isIterable(returnTypeName)) {
      returnTypeName = returnTypeName.annotated(AnnotationSpec.builder(NotEmpty.class).build());
    }

    TypeName fieldTypeName;
    if (nullable && !isIterable(returnTypeName)) {
      fieldTypeName =
          ((ParameterizedTypeName) TypeName.get(attributeElement.getReturnType()))
              .typeArguments.get(0);
    } else {
      fieldTypeName = returnTypeName;
    }
    String fieldName = getFieldName(attributeAnnotation, attributeElement);

    FieldSpec fieldSpec =
        FieldSpec.builder(fieldTypeName.withoutAnnotations(), fieldName, Modifier.PRIVATE).build();
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
        methodBuilder(methodName)
            .addAnnotation(Override.class)
            .addModifiers(methodModifiers)
            .returns(returnTypeName);
    if (nullable && !isIterable(returnTypeName)) {
      methodSpecBuilder.addStatement("return $T.ofNullable($N)", Optional.class, fieldName);
    } else {
      methodSpecBuilder.addStatement("return $N", fieldName);
    }

    MethodSpec methodSpec = methodSpecBuilder.build();
    methodSpecs.add(methodSpec);
  }

  private void handleAbstractAttributeSetter(
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

    TypeName parameterTypeName = TypeName.get(parameterElement.asType());
    ParameterSpec.Builder parameterSpecBuilder =
        ParameterSpec.builder(parameterTypeName, parameterName);
    if (attribute.nullable()) {
      // TODO validate that attribute method has nullable/notempty methods and use abstract method
      // types
      if (!isIterable(parameterTypeName)) {
        parameterSpecBuilder.addAnnotation(Nullable.class);
      } else {
        parameterSpecBuilder.addAnnotation(NotEmpty.class);
      }
    }
    ParameterSpec parameterSpec = parameterSpecBuilder.build();
    MethodSpec methodSpec =
        methodBuilder(methodName)
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

  private boolean isIterable(TypeName typeName) {
    ClassName iterableClassName = ClassName.get(Iterable.class);
    boolean isIterable =
        typeName.withoutAnnotations().equals(iterableClassName.withoutAnnotations());
    if (!isIterable && typeName instanceof ParameterizedTypeName) {
      ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName) typeName;
      isIterable =
          parameterizedTypeName
              .rawType
              .withoutAnnotations()
              .equals(iterableClassName.withoutAnnotations());
    }
    return isIterable;
  }
}
