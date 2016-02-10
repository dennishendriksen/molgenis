package org.molgenis.data.meta;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL_MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.EMAIL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.HTML;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.HYPERLINK;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.SCRIPT;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.TEXT;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DATA_TYPE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.EXPRESSION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.IDENTIFIER;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VALIDATION_EXPRESSION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VISIBLE_EXPRESSION;

import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.SystemEntityMetaDataRegistry;

public class AttributeMetaDataRepositoryDecorator implements Repository
{
	private final Repository decoratedRepo;
	private final SystemEntityMetaDataRegistry systemEntityMetaDataRegistry;

	public AttributeMetaDataRepositoryDecorator(Repository decoratedRepo,
			SystemEntityMetaDataRegistry systemEntityMetaDataRegistry)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.systemEntityMetaDataRegistry = requireNonNull(systemEntityMetaDataRegistry);
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepo.getCapabilities();
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepo.close();
	}

	@Override
	public String getName()
	{
		return decoratedRepo.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepo.getEntityMetaData();
	}

	@Override
	public long count()
	{
		return decoratedRepo.count();
	}

	@Override
	public Query query()
	{
		return decoratedRepo.query();
	}

	@Override
	public long count(Query q)
	{
		return decoratedRepo.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		return decoratedRepo.findAll(q);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decoratedRepo.iterator();
	}

	@Override
	public Entity findOne(Query q)
	{
		return decoratedRepo.findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		return decoratedRepo.findOne(id);
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		return decoratedRepo.findOne(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return decoratedRepo.findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decoratedRepo.findAll(ids, fetch);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decoratedRepo.aggregate(aggregateQuery);
	}

	@Override
	public void update(Entity entity)
	{
		validateUpdateAllowed(entity);
		validateUpdate(entity);
		decoratedRepo.update(entity);
	}

	@Override
	public void update(Stream<? extends Entity> entities)
	{
		decoratedRepo.update(entities.filter(entity -> {
			validateUpdateAllowed(entity);
			validateUpdate(entity);
			return true;
		}));
	}

	@Override
	public void delete(Entity entity)
	{
		validateDeleteAllowed(entity);
		decoratedRepo.delete(entity);
	}

	@Override
	public void delete(Stream<? extends Entity> entities)
	{
		decoratedRepo.delete(entities.filter(entity -> {
			validateDeleteAllowed(entity);
			return true;
		}));
	}

	@Override
	public void deleteById(Object id)
	{
		validateDeleteAllowed(findOne(id));
		decoratedRepo.deleteById(id);
	}

	@Override
	public void deleteById(Stream<Object> ids)
	{
		decoratedRepo.deleteById(ids.filter(id -> {
			validateDeleteAllowed(findOne(id));
			return true;
		}));
	}

	@Override
	public void deleteAll()
	{
		iterator().forEachRemaining(this::validateDeleteAllowed);
		decoratedRepo.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		decoratedRepo.add(entity);
	}

	@Override
	public Integer add(Stream<? extends Entity> entities)
	{
		return decoratedRepo.add(entities);
	}

	@Override
	public void flush()
	{
		decoratedRepo.flush();
	}

	@Override
	public void clearCache()
	{
		decoratedRepo.clearCache();
	}

	@Override
	public void create()
	{
		decoratedRepo.create();
	}

	@Override
	public void drop()
	{
		decoratedRepo.drop();
	}

	@Override
	public void rebuildIndex()
	{
		decoratedRepo.rebuildIndex();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decoratedRepo.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decoratedRepo.removeEntityListener(entityListener);
	}

	private void validateUpdate(Entity newEntity)
	{
		Entity currentEntity = findOne(newEntity.getString(IDENTIFIER));

		// data type
		String currentDataType = currentEntity.getString(DATA_TYPE);
		String newDataType = newEntity.getString(DATA_TYPE);
		if (!Objects.equals(currentDataType, newDataType))
		{
			validateUpdateDataType(currentDataType, newDataType);
		}

		// expression
		String currentExpression = currentEntity.getString(EXPRESSION);
		String newExpression = newEntity.getString(EXPRESSION);
		if (!Objects.equals(currentExpression, newExpression))
		{
			validateUpdateExpression(currentExpression, newExpression);
		}

		String currentValidationExpression = currentEntity.getString(VALIDATION_EXPRESSION);
		String newValidationExpression = newEntity.getString(VALIDATION_EXPRESSION);
		if (!Objects.equals(currentValidationExpression, newValidationExpression))
		{
			validateUpdateExpression(currentValidationExpression, newValidationExpression);
		}

		String currentVisibleExpression = currentEntity.getString(VISIBLE_EXPRESSION);
		String newVisibleExpression = newEntity.getString(VISIBLE_EXPRESSION);
		if (!Objects.equals(currentVisibleExpression, newVisibleExpression))
		{
			validateUpdateExpression(currentVisibleExpression, newVisibleExpression);
		}
	}

	private static EnumMap<FieldTypeEnum, EnumSet<FieldTypeEnum>> DATA_TYPE_ALLOWED_TRANSITIONS;

	static
	{
		DATA_TYPE_ALLOWED_TRANSITIONS = new EnumMap<>(FieldTypeEnum.class);
		DATA_TYPE_ALLOWED_TRANSITIONS.put(CATEGORICAL, EnumSet.of(XREF));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(CATEGORICAL_MREF, EnumSet.of(MREF));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(EMAIL, EnumSet.of(STRING));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(HTML, EnumSet.of(TEXT));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(HYPERLINK, EnumSet.of(STRING));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(MREF, EnumSet.of(CATEGORICAL_MREF));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(SCRIPT, EnumSet.of(TEXT));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(TEXT, EnumSet.of(SCRIPT));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(XREF, EnumSet.of(CATEGORICAL));
	}

	private void validateUpdateDataType(String currentDataTypeStr, String newDataTypeStr)
	{
		FieldTypeEnum currentDataType = FieldTypeEnum.get(currentDataTypeStr);
		FieldTypeEnum newDataType = FieldTypeEnum.get(newDataTypeStr);
		EnumSet<FieldTypeEnum> allowedDataTypes = DATA_TYPE_ALLOWED_TRANSITIONS.get(currentDataType);
		if (allowedDataTypes == null || !allowedDataTypes.contains(newDataType))
		{
			throw new MolgenisDataException(format("Attribute data type update from [%s] to [%s] not allowed",
					currentDataTypeStr, newDataTypeStr));
		}
	}

	private void validateUpdateExpression(String currentExpression, String newExpression)
	{
		// TODO validate with script evaluator
		// how to get access to expression validator here since it is located in molgenis-data-validation?
	}

	/**
	 * Updating attribute meta data is allowed for non-system attributes. For system attributes updating attribute meta
	 * data is only allowed if the meta data defined in Java differs from the meta data stored in the database (in other
	 * words the Java code was updated).
	 * 
	 * @param attrEntity
	 */
	private void validateUpdateAllowed(Entity attrEntity)
	{
		String attrIdentifier = attrEntity.getString(AttributeMetaDataMetaData.IDENTIFIER);
		AttributeMetaData systemAttr = systemEntityMetaDataRegistry.getSystemAttribute(attrIdentifier);
		if (systemAttr != null && !MetaUtils.equals(attrEntity, systemAttr))
		{
			throw new MolgenisDataException(format("Updating system entity attribute [%s] is not allowed",
					attrEntity.getString(AttributeMetaDataMetaData.NAME)));
		}
	}

	/**
	 * Deleting attribute meta data is allowed for non-system attributes.
	 * 
	 * @param attrEntity
	 */
	private void validateDeleteAllowed(Entity attrEntity)
	{
		String attrIdentifier = attrEntity.getString(AttributeMetaDataMetaData.IDENTIFIER);
		AttributeMetaData systemAttr = systemEntityMetaDataRegistry.getSystemAttribute(attrIdentifier);
		if (systemAttr != null)
		{
			throw new MolgenisDataException(format("Deleting system entity attribute [%s] is not allowed",
					attrEntity.getString(AttributeMetaDataMetaData.NAME)));
		}
	}
}
