package org.molgenis.data.entity;

import static java.lang.String.format;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.MolgenisFieldTypes.DATE;
import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.data.support.ExpressionEvaluatorFactory.createExpressionEvaluator;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityCollection;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.fieldtypes.TextField;
import org.molgenis.util.EntityUtils;

public class EntityImpl implements Entity
{
	private static final long serialVersionUID = 1L;

	private static final IdGenerator ID_GENERATOR = new UuidGenerator();

	/**
	 * Entity metadata
	 */
	private final EntityMetaData entityMeta;

	/**
	 * Maps attribute names to values
	 */
	private final Map<String, Object> values;

	EntityImpl(EntityMetaData entityMeta, Map<String, Object> values)
	{
		this.entityMeta = entityMeta;
		this.values = values;

		initialize();
		validate();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMeta;
	}

	@Override
	public Object getIdValue()
	{
		return get(entityMeta.getIdAttribute());
	}

	@Override
	public Object get(AttributeMetaData attr)
	{
		return values.get(attr.getName());
	}

	@Override
	public Object get(String attrName)
	{
		return get(getAttribute(attrName));
	}

	@Override
	public String getString(AttributeMetaData attr)
	{
		return (String) get(attr);
	}

	@Override
	public String getString(String attrName)
	{
		return getString(getAttribute(attrName));
	}

	@Override
	public Integer getInt(AttributeMetaData attr)
	{
		return (Integer) get(attr);
	}

	@Override
	public Integer getInt(String attrName)
	{
		return getInt(getAttribute(attrName));
	}

	@Override
	public Long getLong(AttributeMetaData attr)
	{
		return (Long) get(attr);
	}

	@Override
	public Long getLong(String attrName)
	{
		return getLong(getAttribute(attrName));
	}

	@Override
	public Boolean getBoolean(AttributeMetaData attr)
	{
		return (Boolean) get(attr);
	}

	@Override
	public Boolean getBoolean(String attrName)
	{
		return getBoolean(getAttribute(attrName));
	}

	@Override
	public Double getDouble(AttributeMetaData attr)
	{
		return (Double) get(attr);
	}

	@Override
	public Double getDouble(String attrName)
	{
		return getDouble(getAttribute(attrName));
	}

	@Override
	public Date getDate(AttributeMetaData attr)
	{
		Date date = (Date) get(attr);
		return date != null ? new Date(date.getTime()) : null; // Date is mutable, return new date
	}

	@Override
	public Date getDate(String attrName)
	{
		return getDate(getAttribute(attrName));
	}

	@Override
	public Entity getEntity(AttributeMetaData attr)
	{
		return (Entity) get(attr);
	}

	@Override
	public Entity getEntity(String attrName)
	{
		return getEntity(getAttribute(attrName));
	}

	@Override
	public <E extends Entity> E getEntity(AttributeMetaData attr, Class<E> clazz)
	{
		Entity entity = getEntity(attr);
		return entity != null ? EntityUtils.convert(this, clazz) : null;
	}

	@Override
	public <E extends Entity> E getEntity(String attrName, Class<E> clazz)
	{
		return getEntity(getAttribute(attrName), clazz);
	}

	@Override
	public EntityCollection getEntities(AttributeMetaData attr)
	{
		return (EntityCollection) get(attr);
	}

	@Override
	public EntityCollection getEntities(String attrName)
	{
		return getEntities(getAttribute(attrName));
	}

	private void initialize()
	{
		entityMeta.getAtomicAttributes().forEach(attr -> {
			// generate auto values
			if (attr.isAuto())
			{
				Object value = values.get(attr.getName());
				if (value == null)
				{
					if (attr.isIdAtrribute())
					{
						if (!attr.getDataType().equals(STRING))
						{
							throw new MolgenisDataException(
									format("Expected auto id attribute with type STRING instead of [%s]",
											attr.getDataType().getEnumType()));
						}
						values.put(attr.getName(), ID_GENERATOR.generateId());
					}
					else if (attr.getDataType().equals(DATE))
					{
						values.put(attr.getName(), new Date());
					}
					else if (attr.getDataType().equals(DATETIME))
					{
						values.put(attr.getName(), new Date());
					}
				}
			}

			// set default values
			String defaultValue = attr.getDefaultValue();
			if (defaultValue != null)
			{
				Object value = values.get(attr.getName());
				if (value != null)
				{
					if (!(attr.getDataType() instanceof StringField) && !(attr.getDataType() instanceof TextField))
					{
						throw new MolgenisDataException(
								format("Unexpected attribute type [%s] for attribute with default value [%s]",
										attr.getDataType().getEnumType(), defaultValue));
					}
					values.put(attr.getName(), defaultValue);
				}
			}

			// set values by evaluating expression
			if (attr.getExpression() != null)
			{
				Object computedValue = createExpressionEvaluator(attr, entityMeta).evaluate(this);
				values.put(attr.getName(), computedValue);
			}
		});
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(entityMeta.getName()).append('[');
		builder.append(stream(entityMeta.getAtomicAttributes().spliterator(), false)
				.map(attr -> "{" + attr.getName() + ":" + values.get(attr.getName()) + "}")
				.collect(Collectors.joining(",")));
		builder.append(']');
		return builder.toString();
	}

	private AttributeMetaData getAttribute(String attrName)
	{
		AttributeMetaData attr = getEntityMetaData().getAttribute(attrName);
		if (attr == null)
		{
			throw new UnknownAttributeException(format("Unknown attribute [%s]", attrName));
		}
		return attr;
	}

	private void validate()
	{
		// FIXME add validation
		entityMeta.getAtomicAttributes().forEach(attr -> {
			if (attr.getDataType().equals(INT) && !(values.get(attr.getName()) instanceof Integer))
			{
				throw new MolgenisDataException(format("Attribute [%s] value [%s] is of type %s instead of INTEGER",
						attr.getName(), values.get(attr.getName()).toString(), attr.getDataType().getEnumType()));
			}
		});
	}
}