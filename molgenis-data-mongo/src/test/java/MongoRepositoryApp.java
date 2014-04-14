import java.io.IOException;
import java.net.UnknownHostException;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mongo.MongoRepository;
import org.molgenis.data.support.MapEntity;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class MongoRepositoryApp
{
	public static void main(String[] args) throws UnknownHostException
	{
		MongoClient mongoClient = new MongoClient();
		try
		{
			DB db = mongoClient.getDB("mydb");
			DBCollection collection = db.getCollection("mycollection");
			CrudRepository repo = new MongoRepository(collection, new EntityMetaData()
			{

				@Override
				public String getName()
				{
					return "mydb";
				}

				@Override
				public AttributeMetaData getLabelAttribute()
				{
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getLabel()
				{
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public AttributeMetaData getIdAttribute()
				{
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Class<? extends Entity> getEntityClass()
				{
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getDescription()
				{
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Iterable<AttributeMetaData> getAttributes()
				{
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public AttributeMetaData getAttribute(String attributeName)
				{
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Iterable<AttributeMetaData> getAtomicAttributes()
				{
					// TODO Auto-generated method stub
					return null;
				}
			});
			try
			{
				MapEntity mapEntity = new MapEntity();
				mapEntity.set("key1", "val1");
				mapEntity.set("key2", "val2");
				mapEntity.set("key3", "val3");
				repo.add(mapEntity);

				for (Entity entity : repo)
				{
					System.out.println(entity);
				}
			}
			finally
			{
				try
				{
					repo.close();
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		finally
		{
			mongoClient.close();
		}
	}
}
