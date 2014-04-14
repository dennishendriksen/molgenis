package org.molgenis.data.mongo;

import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

class MongoCollectionIterator implements Iterator<Entity>
{
	private final DBCursor dbCursor;
	private final EntityMetaData entityMetaData;

	public MongoCollectionIterator(DBCursor dbCursor, EntityMetaData entityMetaData)
	{
		this.dbCursor = dbCursor;
		this.entityMetaData = entityMetaData;
	}

	@Override
	public boolean hasNext()
	{
		return dbCursor.hasNext();
	}

	@Override
	public Entity next()
	{
		DBObject dbObject = dbCursor.next();
		return new MongoDocumentEntity(dbObject, entityMetaData);
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
