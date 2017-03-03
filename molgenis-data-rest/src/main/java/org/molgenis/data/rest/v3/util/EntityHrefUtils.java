package org.molgenis.data.rest.v3.util;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rest.v3.RestControllerV3;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

public class EntityHrefUtils
{
	public static URI getEntityHref(Entity entity)
	{
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(RestControllerV3.URI)
				.pathSegment(entity.getEntityType().getFullyQualifiedName(), entity.getIdValue().toString()).build()
				.toUri();
	}

	public static URI getEntityTypeHref(EntityType entityType)
	{
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(RestControllerV3.URI)
				.path(entityType.getFullyQualifiedName()).build().toUri();
	}
}
