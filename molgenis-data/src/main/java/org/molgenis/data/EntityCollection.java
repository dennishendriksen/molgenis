package org.molgenis.data;

import java.util.Iterator;

public interface EntityCollection extends Iterable<Entity>
{
	EntityMetaData getEntityMetaData();

	<E extends Entity> Iterator<E> iterator(Class<E> clazz);

	Iterable<String> getAttributeNames();

	/**
	 * Returns whether this entity collection is lazy, i.e. all entities are references to entities (= lazy entities)
	 * 
	 * @return whether this entity collection is lazy
	 */
	public boolean isLazy();
}
