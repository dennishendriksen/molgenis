package org.molgenis.data.validation;

import org.molgenis.data.Entity;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public class EntityValidationException extends EntityCollectionValidationException
{
	public EntityValidationException(EntityValidationErrors<? extends Entity> entityValidationErrors)
	{
		super(singletonList(requireNonNull(entityValidationErrors)));
	}
}
