package org.molgenis.data.validation;

import org.molgenis.data.Entity;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public class EntityCollectionValidationException extends RuntimeException
{
	private final transient List<EntityValidationErrors<? extends Entity>> entityValidationErrors;

	public EntityCollectionValidationException(List<EntityValidationErrors<? extends Entity>> entityValidationErrors)
	{
		this.entityValidationErrors = requireNonNull(entityValidationErrors);
	}

	public List<EntityValidationErrors<? extends Entity>> getEntityValidationErrors()
	{
		return entityValidationErrors;
	}

	@Override
	public String getMessage()
	{
		return entityValidationErrors.stream().map(EntityValidationErrors::toString).collect(joining(", "));
	}
}
