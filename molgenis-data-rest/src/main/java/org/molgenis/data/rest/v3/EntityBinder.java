package org.molgenis.data.rest.v3;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rest.v3.model.CreateEntitiesRequest;
import org.molgenis.data.rest.v3.model.CreateEntityRequest;

import java.util.List;

public interface EntityBinder
{
	Entity toEntity(EntityType entityType, CreateEntityRequest createEntityRequest);

	List<Entity> toEntities(EntityType entityType, CreateEntitiesRequest createEntitiesRequest);
}
