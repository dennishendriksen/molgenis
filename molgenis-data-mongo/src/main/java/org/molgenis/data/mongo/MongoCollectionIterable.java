package org.molgenis.data.mongo;

import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

class MongoCollectionIterable implements Iterable<Entity>
{
	private final DBCollection dbCollection;
	private final EntityMetaData entityMetaData;

	public MongoCollectionIterable(DBCollection dbCollection, EntityMetaData entityMetaData)
	{
		this.dbCollection = dbCollection;
		this.entityMetaData = entityMetaData;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		DBCursor dbCursor = dbCollection.find();
		return new MongoCollectionIterator(dbCursor, entityMetaData);
	}
}
