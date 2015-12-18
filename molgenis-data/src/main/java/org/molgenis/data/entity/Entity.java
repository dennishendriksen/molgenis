package org.molgenis.data.entity;

import java.io.Serializable;
import java.util.Date;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityCollection;
import org.molgenis.data.EntityMetaData;

public interface Entity extends Serializable
{
	EntityMetaData getEntityMetaData();

	Object getIdValue();

	Object get(AttributeMetaData attr);

	Object get(String attrName);

	String getString(AttributeMetaData attr);

	String getString(String attrName);

	Integer getInt(AttributeMetaData attr);

	Integer getInt(String attrName);

	Long getLong(AttributeMetaData attr);

	Long getLong(String attrName);

	Boolean getBoolean(AttributeMetaData attr);

	Boolean getBoolean(String attrName);

	Double getDouble(AttributeMetaData attr);

	Double getDouble(String attrName);

	Date getDate(AttributeMetaData attr);

	Date getDate(String attrName);

	Entity getEntity(AttributeMetaData attr);

	Entity getEntity(String attrName);

	<E extends Entity> E getEntity(AttributeMetaData attr, Class<E> clazz);

	<E extends Entity> E getEntity(String attrName, Class<E> clazz);

	EntityCollection getEntities(AttributeMetaData attr);

	EntityCollection getEntities(String attrName);
}