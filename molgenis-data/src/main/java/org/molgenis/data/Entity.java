package org.molgenis.data;

import java.io.Serializable;

/**
 * Entity is a data record which can contain a hash of attribute values. Attribute names are unique. Synonyms are
 * ‘tuple’, ‘record’, ‘row’, ‘hashmap’. Optionally Entity can provide a unique ‘id’ for updates. Optionally Entity can
 * provide a human readable label for lookups
 */
public interface Entity extends Serializable
{
	EntityMetaData getEntityMetaData();

	/**
	 * Optional unique id to identify this Entity. Otherwise return null
	 */
	Object getIdValue();

	/**
	 * Get attribute value
	 */
	Object get(AttributeMetaData attr);

	/**
	 * Retrieves the value of the designated column as String.
	 */
	String getString(AttributeMetaData attr);

	/**
	 * Retrieves the value of the designated column as Integer.
	 */
	Integer getInt(AttributeMetaData attr);

	/**
	 * Retrieves the value of the designated column as Long.
	 */
	Long getLong(AttributeMetaData attr);

	/**
	 * Retrieves the value of the designated column as Boolean.
	 */
	Boolean getBoolean(AttributeMetaData attr);

	/**
	 * Retrieves the value of the designated column as Double.
	 */
	Double getDouble(AttributeMetaData attr);

	/**
	 * Retrieves the value of the designated column as {@link java.util.Date}.
	 */
	java.util.Date getUtilDate(AttributeMetaData attr);

	/**
	 * Retrieves the value of the designated column as entity
	 */
	Entity getEntity(AttributeMetaData attr);

	/**
	 * Retrieves the value of the designated column as entity of the give type
	 */
	<E extends Entity> E getEntity(AttributeMetaData attr, Class<E> clazz);

	/**
	 * Retrieves the value of the designated column as a entity iterable
	 */
	EntityCollection getEntities(AttributeMetaData attr);

	/**
	 * Change attribute value
	 */
	void set(AttributeMetaData attr, Object value);

	/**
	 * Copy attribute values from another entity
	 */
	void set(Entity entity);
}
