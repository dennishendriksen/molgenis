package org.molgenis.data.mongo;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import org.bson.types.ObjectId;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoRepository implements CrudRepository
{
	public static final String BASE_URL = "mongo://";

	private final DBCollection dbCollection;
	private final EntityMetaData entityMetaData;
	private final String entityName;

	public MongoRepository(DBCollection dbCollection, EntityMetaData entityMetaData) throws UnknownHostException
	{
		this.dbCollection = dbCollection;
		this.entityMetaData = entityMetaData;
		this.entityName = entityMetaData.getName();
	}

	@Override
	public String getName()
	{
		return entityName;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		if (!clazz.equals(MongoDocumentEntity.class))
		{
			throw new IllegalArgumentException("invalid class [" + clazz.getName() + "]");
		}
		return (Iterable<E>) new MongoCollectionIterable(dbCollection, entityMetaData);
	}

	@Override
	public String getUrl()
	{
		return BASE_URL + entityName + '/';
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new MongoCollectionIterable(dbCollection, entityMetaData).iterator();
	}

	@Override
	public void close() throws IOException
	{
		// noop
	}

	@Override
	public long count(Query q)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity findOne(Query q)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity findOne(Integer id)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Integer> ids)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Integer> ids, Class<E> clazz)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends Entity> E findOne(Integer id, Class<E> clazz)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void update(Entity entity)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Entity entity)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteById(Integer id)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteById(Iterable<Integer> ids)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAll()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Integer add(Entity entity)
	{
		DBObject doc = EntityToMongoDocumentConverter.convert(entity);
		dbCollection.insert(doc);
		ObjectId id = (ObjectId) doc.get(MongoConstants.ID_FIELD);
		return null;
		// return id.toHexString(); // FIXME
	}

	@Override
	public void add(Iterable<? extends Entity> entities)
	{
		for (Entity entity : entities)
			add(entity);
	}

	@Override
	public void flush()
	{
		// noop
	}

	@Override
	public void clearCache()
	{
		// noop
	}
}