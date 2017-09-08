package org.molgenis.data.rest.v3;

import org.molgenis.data.rest.v3.model.*;

public interface RestService
{
	CreateEntityResponse createEntity(String entityTypeId, CreateEntityRequest entityRequest);

	CreateEntitiesResponse createEntities(String entityTypeId, CreateEntitiesRequest entitiesRequest);

	ReadEntityResponse readEntity(String entityTypeId, String entityId, ReadEntityRequest entityRequest);

	ReadEntitiesResponse readEntities(String entityTypeId, ReadEntitiesRequest entitiesRequest);

	void updateEntity(String entityTypeId, String entityId, UpdateEntityRequest entityRequest);

	void updateEntities(String entityTypeId, UpdateEntitiesRequest entitiesRequest);

	void partialUpdateEntity(String entityTypeId, String entityId, PartialUpdateEntityRequest entityRequest);

	void partialUpdateEntities(String entityTypeId, PartialUpdateEntitiesRequest entitiesRequest);

	void deleteEntity(String entityTypeId, String entityId);

	void deleteEntities(String entityTypeId, DeleteEntitiesRequest entitiesRequest);
}
