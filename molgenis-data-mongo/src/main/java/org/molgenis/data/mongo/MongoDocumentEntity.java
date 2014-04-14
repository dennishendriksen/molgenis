package org.molgenis.data.mongo;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.mongodb.DBObject;

public class MongoDocumentEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	private final DBObject dbObject;
	private final EntityMetaData entityMetaData;

	public MongoDocumentEntity(DBObject dbObject, EntityMetaData entityMetaData)
	{
		this.dbObject = dbObject;
		this.entityMetaData = entityMetaData;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return Iterables.transform(entityMetaData.getAttributes(), new Function<AttributeMetaData, String>()
		{
			@Override
			public String apply(AttributeMetaData attributeMetaData)
			{
				return attributeMetaData.getName();
			}
		});
	}

	@Override
	public Integer getIdValue()
	{
		ObjectId objectId = (ObjectId) dbObject.get(MongoConstants.ID_FIELD);
		return null;
		// return objectId.toHexString(); // FIXME
	}

	@Override
	public String getLabelValue()
	{
		AttributeMetaData labelAttribute = entityMetaData.getLabelAttribute();
		return labelAttribute != null ? getString(labelAttribute.getName()) : null;
	}

	@Override
	public List<String> getLabelAttributeNames()
	{
		return Arrays.asList(entityMetaData.getLabelAttribute().getName());
	}

	@Override
	public Object get(String attributeName)
	{
		return dbObject.get(attributeName);
	}

	@Override
	public String getString(String attributeName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getInt(String attributeName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getLong(String attributeName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getDouble(String attributeName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getDate(String attributeName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public java.util.Date getUtilDate(String attributeName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Timestamp getTimestamp(String attributeName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity getEntity(String attributeName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getList(String attributeName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> getIntList(String attributeName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void set(String attributeName, Object value)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void set(Entity values)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void set(Entity entity, boolean strict)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String toString()
	{
		return dbObject.toString();
	}
}
