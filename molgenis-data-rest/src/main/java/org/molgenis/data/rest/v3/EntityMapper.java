package org.molgenis.data.rest.v3;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rest.v3.model.ReadEntitiesResponse;
import org.molgenis.data.rest.v3.model.ReadEntityResponse;

import java.util.stream.Stream;

public interface EntityMapper
{
	ReadEntityResponse toReadEntityResponse(Entity entity);

	ReadEntitiesResponse toReadEntitiesResponse(EntityType entityType, Stream<Entity> entities);
}
