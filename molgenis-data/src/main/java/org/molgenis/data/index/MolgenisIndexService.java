package org.molgenis.data.index;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

public interface MolgenisIndexService
{
	public static final String DEFAULT_INDEX_NAME = "molgenis";

	public MolgenisIndexUtil getMolgenisIndexUtil();

	public void delete(String entityName);

	public void createMappings(EntityMetaData entityMetaData);

	public void rebuildIndex(Iterable<? extends Entity> entities, EntityMetaData entityMetaData);

	public void add(Iterable<? extends Entity> entities, EntityMetaData entityMetaData);
}
