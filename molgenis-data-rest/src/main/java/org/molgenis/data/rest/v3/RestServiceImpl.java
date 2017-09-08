package org.molgenis.data.rest.v3;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rest.v3.model.*;
import org.molgenis.data.rest.v3.util.EntityHrefUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Service
public class RestServiceImpl implements RestService
{
	private final DataService dataService;
	private final EntityMapper entityMapper;
	private final EntityBinder entityBinder;

	@Autowired
	public RestServiceImpl(DataService dataService, EntityMapper entityMapper, EntityBinder entityBinder)
	{
		this.dataService = requireNonNull(dataService);
		this.entityMapper = requireNonNull(entityMapper);
		this.entityBinder = requireNonNull(entityBinder);
	}

	@Override
	public CreateEntityResponse createEntity(String entityTypeId, CreateEntityRequest entityRequest)
	{
		EntityType entityType = dataService.getEntityType(entityTypeId);
		Entity entity = entityBinder.toEntity(entityType, entityRequest);
		dataService.add(entityTypeId, entity);
		return CreateEntityResponse.builder().setLocation(EntityHrefUtils.getEntityHref(entity)).build();
	}

	@Override
	public CreateEntitiesResponse createEntities(String entityTypeId, CreateEntitiesRequest entitiesRequest)
	{
		EntityType entityType = dataService.getEntityType(entityTypeId);
		List<Entity> entities = entityBinder.toEntities(entityType, entitiesRequest);
		dataService.add(entityTypeId, entities.stream());
		List<URI> locations = entities.stream().map(EntityHrefUtils::getEntityHref).collect(toList());
		return CreateEntitiesResponse.builder().setLocations(locations).build();
	}

	@Override
	public ReadEntityResponse readEntity(String entityTypeId, String entityId, ReadEntityRequest entityRequest)
	{
		Entity entity = dataService.findOneById(entityTypeId, entityId);
		if (entity == null)
		{
			throw new UnknownEntityException(format("Unknown entity '%s' of type '%s'", entityId, entityTypeId));
		}
		return entityMapper.toReadEntityResponse(entity);
	}

	@Override
	public ReadEntitiesResponse readEntities(String entityTypeId, ReadEntitiesRequest entitiesRequest)
	{
		EntityType entityType = dataService.getEntityType(entityTypeId);
		Stream<Entity> entities = dataService.findAll(entityTypeId);
		return entityMapper.toReadEntitiesResponse(entityType, entities);
	}

	@Override
	public void updateEntity(String entityTypeId, String entityId, UpdateEntityRequest entityRequest)
	{
		throw new UnsupportedOperationException(); // TODO implement
	}

	@Override
	public void updateEntities(String entityTypeId, UpdateEntitiesRequest entitiesRequest)
	{
		throw new UnsupportedOperationException(); // TODO implement
	}

	@Override
	public void partialUpdateEntity(String entityTypeId, String entityId, PartialUpdateEntityRequest entityRequest)
	{
		throw new UnsupportedOperationException(); // TODO implement
	}

	@Override
	public void partialUpdateEntities(String entityTypeId, PartialUpdateEntitiesRequest entitiesRequest)
	{
		throw new UnsupportedOperationException(); // TODO implement
	}

	@Override
	public void deleteEntity(String entityTypeId, String entityId)
	{
		dataService.deleteById(entityTypeId, entityId);
	}

	@Override
	public void deleteEntities(String entityTypeId, DeleteEntitiesRequest entitiesRequest)
	{
		Stream<Object> ids = entitiesRequest.getIds().stream().map(id -> (Object) id);
		dataService.deleteAll(entityTypeId, ids);
	}
}
