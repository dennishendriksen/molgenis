package org.molgenis.data;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Streams;
import java.time.Instant;
import java.time.LocalDate;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

public abstract class AbstractEntity implements Entity {
  private final EntityType entityType;

  public AbstractEntity(EntityType entityType) {
    this.entityType = requireNonNull(entityType);
  }

  @Override
  public EntityType getEntityType() {
    return entityType;
  }

  @Override
  public Iterable<String> getAttributeNames() {
    return Streams.stream(entityType.getAtomicAttributes()).map(Attribute::getName)::iterator;
  }

  // copied from DynamicEntity.getIdValue
  @Override
  public Object getIdValue() {
    Attribute idAttr = entityType.getIdAttribute();
    return idAttr != null ? get(idAttr.getName()) : null;
  }

  // copied from DynamicEntity.setIdValue
  @Override
  public void setIdValue(Object id) {
    Attribute idAttr = entityType.getIdAttribute();
    if (idAttr == null) {
      throw new IllegalArgumentException(
          format("Entity [%s] doesn't have an id attribute", entityType.getId()));
    }
    set(idAttr.getName(), id);
  }

  // copied from DynamicEntity.getLabelValue
  @Override
  public Object getLabelValue() {
    // abstract entities might not have an label attribute
    Attribute labelAttr = entityType.getLabelAttribute();
    return labelAttr != null ? get(labelAttr.getName()) : null;
  }

  @Override
  public String getString(String attrName) {
    return (String) get(attrName);
  }

  @Override
  public Integer getInt(String attrName) {
    return (Integer) get(attrName);
  }

  @Override
  public Long getLong(String attrName) {
    return (Long) get(attrName);
  }

  @Override
  public Boolean getBoolean(String attrName) {
    return (Boolean) get(attrName);
  }

  @Override
  public Double getDouble(String attrName) {
    return (Double) get(attrName);
  }

  @Override
  public Instant getInstant(String attrName) {
    return (Instant) get(attrName);
  }

  @Override
  public LocalDate getLocalDate(String attrName) {
    return (LocalDate) get(attrName);
  }

  @Override
  public Entity getEntity(String attrName) {
    return (Entity) get(attrName);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <E extends Entity> E getEntity(String attrName, Class<E> clazz) {
    return (E) get(attrName);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterable<Entity> getEntities(String attrName) {
    Object value = get(attrName);
    return value != null ? (Iterable<Entity>) value : emptyList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <E extends Entity> Iterable<E> getEntities(String attrName, Class<E> clazz) {
    Object value = get(attrName);
    return value != null ? (Iterable<E>) value : emptyList();
  }

  @Override
  public void set(Entity values) {
    values.getAttributeNames().forEach(attrName -> set(attrName, values.get(attrName)));
  }
}
