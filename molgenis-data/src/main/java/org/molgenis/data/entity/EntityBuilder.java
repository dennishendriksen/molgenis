package org.molgenis.data.entity;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class EntityBuilder
{
	private final EntityMetaData entityMeta;
	private final Map<String, Object> values;

	public EntityBuilder(EntityMetaData entityMeta)
	{
		this.entityMeta = requireNonNull(entityMeta);
		int nrAttrs = Iterables.size(entityMeta.getAtomicAttributes());
		this.values = Maps.newHashMapWithExpectedSize(nrAttrs);
	}

	public EntityBuilder(Entity entity)
	{
		this(entity.getEntityMetaData());
		set(entity);
	}

	public EntityBuilder setId(Object value)
	{
		values.put(entityMeta.getIdAttribute().getName(), value);
		return this;
	}

	public EntityBuilder set(String attrName, Object value)
	{
		values.put(attrName, value);
		return this;
	}

	public EntityBuilder set(AttributeMetaData attr, Object value)
	{
		values.put(attr.getName(), value);
		return this;
	}

	public EntityBuilder set(Entity entity)
	{
		entityMeta.getAtomicAttributes().forEach(attr -> {
			set(attr, entity.get(attr.getName()));
		});
		return this;
	}

	public Entity build()
	{
		return new EntityImpl(entityMeta, values);
	}

	EntityMetaData getEntityMeta()
	{
		return entityMeta;
	}

	Map<String, Object> getValues()
	{
		return values;
	}
}