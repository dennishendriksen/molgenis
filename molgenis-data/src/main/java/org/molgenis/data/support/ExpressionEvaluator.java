package org.molgenis.data.support;

import org.molgenis.data.Entity;

public interface ExpressionEvaluator
{
	public abstract Object evaluate(Entity entity);

	Object evaluate(org.molgenis.data.entity.Entity entity);
}
