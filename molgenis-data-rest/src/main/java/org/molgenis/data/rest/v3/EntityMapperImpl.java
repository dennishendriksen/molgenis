package org.molgenis.data.rest.v3;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rest.v3.model.ReadEntitiesResponse;
import org.molgenis.data.rest.v3.model.ReadEntityResponse;
import org.molgenis.data.rest.v3.model.ResponseEntity;
import org.molgenis.data.rest.v3.util.EntityHrefUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.sql.Date;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.util.MolgenisDateFormat.getDateFormat;
import static org.molgenis.util.MolgenisDateFormat.getDateTimeFormat;

@Component
public class EntityMapperImpl implements EntityMapper
{
	@Override
	public ReadEntityResponse toReadEntityResponse(Entity entity)
	{
		return toReadEntityResponse(entity, true, true);
	}

	@Override
	public ReadEntitiesResponse toReadEntitiesResponse(EntityType entityType, Stream<Entity> entities)
	{
		URI href = EntityHrefUtils.getEntityTypeHref(entityType);
		ResponseEntity responseMeta = toResponseEntity(entityType);
		List<ResponseEntity> responseItems = entities.map(this::toResponseEntity).collect(toList());
		return ReadEntitiesResponse.builder().setHref(href).setMeta(responseMeta).setItems(responseItems).build();
	}

	private ReadEntityResponse toReadEntityResponse(Entity entity, boolean includeMeta, boolean includeData)
	{
		URI href = EntityHrefUtils.getEntityHref(entity);
		ResponseEntity responseMeta = includeMeta ? toResponseEntity(entity.getEntityType()) : null;
		ResponseEntity responseData = includeData ? toResponseEntity(entity) : null;
		return ReadEntityResponse.builder().setHref(href).setMeta(responseMeta).setData(responseData).build();
	}

	private ResponseEntity toResponseEntity(Entity entity)
	{
		ResponseEntity responseEntity = new ResponseEntity();
		entity.getEntityType().getAllAttributes().forEach(attribute ->
		{
			if (attribute.getDataType() != AttributeType.COMPOUND)
			{
				Object responseValue = getResponseValue(entity, attribute);
				responseEntity.put(attribute.getName(), responseValue);
			}
		});
		return responseEntity;
	}

	private Object getResponseValue(Entity entity, Attribute attribute)
	{
		String attributeName = attribute.getName();
		AttributeType attributeType = attribute.getDataType();
		switch (attributeType)
		{
			case BOOL:
				return entity.getBoolean(attributeName);
			case CATEGORICAL:
			case FILE:
			case XREF:
				Entity xrefEntity = entity.getEntity(attributeName);
				return xrefEntity != null ? toReadEntityResponse(xrefEntity, false, false) : null;
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				Iterable<Entity> refEntities = entity.getEntities(attributeName);
				return stream(refEntities.spliterator(), false)
						.map(refEntity -> toReadEntityResponse(refEntity, false, false)).collect(toList());
			case DATE:
				Date date = entity.getDate(attributeName);
				return date != null ? getDateFormat().format(date) : null;
			case DATE_TIME:
				Date dateTime = entity.getDate(attributeName);
				return dateTime != null ? getDateTimeFormat().format(dateTime) : null;
			case DECIMAL:
				return entity.getDouble(attributeName);
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				return entity.getString(attributeName);
			case INT:
				return entity.getInt(attributeName);
			case LONG:
				return entity.getLong(attributeName);
			case COMPOUND:
				throw new RuntimeException(format("Illegal attribute type '%s'", attributeType));
			default:
				throw new RuntimeException(format("Unknown attribute type '%s'", attributeType));
		}
	}
}
