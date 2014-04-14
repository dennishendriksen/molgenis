package org.molgenis.data.mongo;

import org.molgenis.data.Entity;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class EntityToMongoDocumentConverter
{
	public static DBObject convert(Entity entity)
	{
		BasicDBObject doc = new BasicDBObject();
		for (String attributeName : entity.getAttributeNames())
			doc.append(attributeName, entity.get(attributeName));
		return doc;
	}
}
