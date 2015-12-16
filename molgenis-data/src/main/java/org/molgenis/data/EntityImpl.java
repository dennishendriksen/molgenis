package org.molgenis.data;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

import java.util.Date;
import java.util.Map;

import org.molgenis.util.EntityUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class EntityImpl implements Entity
{
	private static final long serialVersionUID = 1L;

	// FIXME EntityMetaData should be immutable
	private final EntityMetaData entityMetaData;
	// TODO replace with List<Object> if AttributeMetaData has a position
	private final Map<AttributeMetaData, Object> values;

	public EntityImpl(EntityMetaData entityMetaData)
	{
		this.entityMetaData = requireNonNull(entityMetaData);
		this.values = Maps.newHashMapWithExpectedSize(Iterables.size(entityMetaData.getAtomicAttributes()));
	}

	public EntityImpl(Entity entity)
	{
		this(entity.getEntityMetaData());
		set(entity);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public Object getIdValue()
	{
		return get(entityMetaData.getIdAttribute());
	}

	@Override
	public Object get(AttributeMetaData attr)
	{
		return values.get(attr);
	}

	@Override
	public String getString(AttributeMetaData attr)
	{
		return (String) get(attr);
	}

	@Override
	public Integer getInt(AttributeMetaData attr)
	{
		return (Integer) get(attr);
	}

	@Override
	public Long getLong(AttributeMetaData attr)
	{
		return (Long) get(attr);
	}

	@Override
	public Boolean getBoolean(AttributeMetaData attr)
	{
		return (Boolean) get(attr);
	}

	@Override
	public Double getDouble(AttributeMetaData attr)
	{
		return (Double) get(attr);
	}

	@Override
	public Date getUtilDate(AttributeMetaData attr)
	{
		return (Date) get(attr);
	}

	@Override
	public Entity getEntity(AttributeMetaData attr)
	{
		return (Entity) get(attr);
	}

	@Override
	public <E extends Entity> E getEntity(AttributeMetaData attr, Class<E> clazz)
	{
		Entity entity = getEntity(attr);
		return entity != null ? EntityUtils.convert(this, clazz) : null;
	}

	@Override
	public EntityCollection getEntities(AttributeMetaData attr)
	{
		return (EntityCollection) get(attr);
	}

	@Override
	public void set(AttributeMetaData attr, Object value)
	{
		if (attr.getExpression() != null)
		{
			throw new MolgenisDataException(
					format("Setting computed attribute [%s] value not allowed", attr.getName()));
		}
		// FIXME valid data only guaranteed if AttributeMetaData (which is mutable) does not change
		attr.getDataType().validate(value);
		values.put(attr, value);
	}

	@Override
	public void set(Entity entity)
	{
		stream(entity.getEntityMetaData().getAtomicAttributes().spliterator(), false)
				.filter(attr -> attr.getExpression() == null).forEach(attr -> {
					Object value = entity.get(attr);
					values.put(attr, value);
				});
	}
}
