package org.molgenis.data.annotation.generator;

import com.squareup.javapoet.FieldSpec;

public class AttributeSpec {
  private final String attributeName;
  private final FieldSpec fieldSpec;
  private final boolean isIdAttribute;
  private final boolean isLabelAttribute;

  public AttributeSpec(
      String attributeName, FieldSpec fieldSpec, boolean isIdAttribute, boolean isLabelAttribute) {
    this.attributeName = attributeName;
    this.fieldSpec = fieldSpec;
    this.isIdAttribute = isIdAttribute;
    this.isLabelAttribute = isLabelAttribute;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public FieldSpec getField() {
    return fieldSpec;
  }

  public boolean isIdAttribute() {
    return isIdAttribute;
  }

  public boolean isLabelAttribute() {
    return isLabelAttribute;
  }
}
