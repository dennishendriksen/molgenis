package org.molgenis.data.rest.v3;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rest.v3.model.CreateEntitiesRequest;
import org.molgenis.data.rest.v3.model.CreateEntityRequest;
import org.molgenis.file.model.FileMeta;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.util.MolgenisDateFormat.getDateFormat;
import static org.molgenis.util.MolgenisDateFormat.getDateTimeFormat;

@Component
public class EntityBinderImpl implements EntityBinder
{
	private final EntityManager entityManager;

	@Autowired
	public EntityBinderImpl(EntityManager entityManager)
	{
		this.entityManager = requireNonNull(entityManager);
	}

	@Override
	public Entity toEntity(EntityType entityType, CreateEntityRequest createEntityRequest)
	{
		Entity entity = entityManager.create(entityType, POPULATE);
		createEntityRequest.forEach((attributeName, value) ->
		{
			Attribute attribute = entityType.getAttribute(attributeName);
			if (attribute == null)
			{
				throw new UnknownAttributeException(
						String.format("Unknown attribute '%s' of entity type '%s'", attributeName,
								entityType.getFullyQualifiedName()));
			}
			Object entityValue = toEntityValue(attribute, value);
			entity.set(attributeName, entityValue);
		});
		return entity;
	}

	@Override
	public List<Entity> toEntities(EntityType entityType, CreateEntitiesRequest createEntitiesRequest)
	{
		return createEntitiesRequest.getEntities().stream()
				.map(createEntityRequest -> toEntity(entityType, createEntityRequest)).collect(toList());
	}

	private Object toEntityValue(Attribute attribute, Object paramValue)
	{
		Object value;
		AttributeType attrType = attribute.getDataType();
		switch (attrType)
		{
			case BOOL:
				value = convertBool(attribute, paramValue);
				break;
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				value = convertString(attribute, paramValue);
				break;
			case CATEGORICAL:
			case XREF:
				value = convertRef(attribute, paramValue);
				break;
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				value = convertMref(attribute, paramValue);
				break;
			case DATE:
				value = convertDate(attribute, paramValue);
				break;
			case DATE_TIME:
				value = convertDateTime(attribute, paramValue);
				break;
			case DECIMAL:
				value = convertDecimal(attribute, paramValue);
				break;
			case FILE:
				value = convertFile(attribute, paramValue);
				break;
			case INT:
				value = convertInt(attribute, paramValue);
				break;
			case LONG:
				value = convertLong(attribute, paramValue);
				break;
			case COMPOUND:
				throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
		return value;
	}

	private static Long convertLong(Attribute attr, Object paramValue)
	{
		Long value;
		if (paramValue != null)
		{
			if (paramValue instanceof String)
			{
				value = Long.valueOf((String) paramValue);
			}
			// javascript number converted to double
			else if (paramValue instanceof Number)
			{
				value = ((Number) paramValue).longValue();
			}
			else
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), String.class.getSimpleName(),
								Number.class.getSimpleName()));
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static Integer convertInt(Attribute attr, Object paramValue)
	{
		Integer value;
		if (paramValue != null)
		{
			if (paramValue instanceof String)
			{
				value = Integer.valueOf((String) paramValue);
			}
			// javascript number converted to double
			else if ((paramValue instanceof Number))
			{
				value = ((Number) paramValue).intValue();
			}
			else
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), String.class.getSimpleName(),
								Number.class.getSimpleName()));
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private FileMeta convertFile(Attribute attr, Object paramValue)
	{
		throw new UnsupportedOperationException("TODO implement");
	}

	private static Double convertDecimal(Attribute attr, Object paramValue)
	{
		Double value;
		if (paramValue != null)
		{
			if (paramValue instanceof String)
			{
				value = Double.valueOf((String) paramValue);
			}
			// javascript number converted to double
			else if (paramValue instanceof Number)
			{
				value = ((Number) paramValue).doubleValue();
			}
			else
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), String.class.getSimpleName(),
								Number.class.getSimpleName()));
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static Date convertDateTime(Attribute attr, Object paramValue)
	{
		Date value;
		if (paramValue != null)
		{
			if (paramValue instanceof Date)
			{
				value = (Date) paramValue;
			}
			else if (paramValue instanceof String)
			{
				String paramStrValue = (String) paramValue;
				try
				{
					value = getDateTimeFormat().parse(paramStrValue);
				}
				catch (ParseException e)
				{
					throw new MolgenisDataException(
							format("Attribute [%s] value [%s] does not match date format [%s]", attr.getName(),
									paramStrValue, MolgenisDateFormat.getDateTimeFormat().toPattern()));
				}
			}
			else
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), String.class.getSimpleName(),
								Date.class.getSimpleName()));
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static Date convertDate(Attribute attr, Object paramValue)
	{
		Date value;
		if (paramValue != null)
		{
			if (paramValue instanceof Date)
			{
				value = (Date) paramValue;
			}
			else if (paramValue instanceof String)
			{
				String paramStrValue = (String) paramValue;
				try
				{
					value = getDateFormat().parse(paramStrValue);
				}
				catch (ParseException e)
				{
					throw new MolgenisDataException(
							format("Attribute [%s] value [%s] does not match date format [%s]", attr.getName(),
									paramStrValue, MolgenisDateFormat.getDateFormat().toPattern()));
				}
			}
			else
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), String.class.getSimpleName()));
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private List<?> convertMref(Attribute attr, Object paramValue)
	{
		List<?> value;
		if (paramValue != null)
		{
			List<?> mrefParamValues;
			if (paramValue instanceof String)
			{
				mrefParamValues = asList(StringUtils.split((String) paramValue, ','));
			}
			else if (paramValue instanceof List<?>)
			{
				mrefParamValues = (List<?>) paramValue;
			}
			else
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), String.class.getSimpleName(),
								List.class.getSimpleName()));
			}

			EntityType mrefEntity = attr.getRefEntity();
			Attribute mrefEntityIdAttr = mrefEntity.getIdAttribute();
			value = mrefParamValues.stream().map(mrefParamValue -> toEntityValue(mrefEntityIdAttr, mrefParamValue))
					.map(mrefIdValue -> entityManager.getReference(mrefEntity, mrefIdValue)).collect(toList());
		}
		else
		{
			value = emptyList();
		}
		return value;
	}

	private Object convertRef(Attribute attr, Object paramValue)
	{
		Object value;
		if (paramValue != null)
		{
			Object idValue = toEntityValue(attr.getRefEntity().getIdAttribute(), paramValue);
			value = entityManager.getReference(attr.getRefEntity(), idValue);
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static String convertString(Attribute attr, Object paramValue)
	{
		String value;
		if (paramValue != null)
		{
			if (paramValue instanceof String)
			{
				value = (String) paramValue;
			}
			else
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), String.class.getSimpleName()));
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static Boolean convertBool(Attribute attr, Object paramValue)
	{
		Boolean value;
		if (paramValue != null)
		{
			if (paramValue instanceof String)
			{
				value = Boolean.valueOf((String) paramValue);
			}
			else if (paramValue instanceof Boolean)
			{
				value = (Boolean) paramValue;
			}
			else
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), String.class.getSimpleName(),
								Boolean.class.getSimpleName()));
			}
		}
		else
		{
			// boolean false is not posted (http feature), so if null and required, should be false
			value = !attr.isNillable() ? false : null;
		}
		return value;
	}
}
