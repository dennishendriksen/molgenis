package org.molgenis.data.annotation.generator;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import java.util.Optional;

public class AttributeSpecs {
  private Map<String, AttributeSpec> attributeSpecs;

  public AttributeSpecs(Map<String, AttributeSpec> attributeSpecs) {
    this.attributeSpecs = attributeSpecs;
  }

  public void put(String attributeName, AttributeSpec attributeSpec) {
    attributeSpecs.put(attributeName, attributeSpec);
  }

  public ImmutableList<AttributeSpec> get() {
    return ImmutableList.copyOf(attributeSpecs.values());
  }

  public Optional<AttributeSpec> getIdAttributeSpec() {
    return attributeSpecs.values().stream().filter(AttributeSpec::isIdAttribute).findFirst();
  }

  public Optional<AttributeSpec> getLabelAttributeSpec() {
    return attributeSpecs.values().stream().filter(AttributeSpec::isLabelAttribute).findFirst();
  }
}
