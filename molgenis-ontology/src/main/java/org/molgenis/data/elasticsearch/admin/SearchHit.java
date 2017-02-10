package org.molgenis.data.elasticsearch.admin;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_SearchHit.class)
public abstract class SearchHit
{
	static SearchHit create(String entityTypeId, String entityTypeLabel, String entityId)
	{
		return new AutoValue_SearchHit(entityTypeId, entityTypeLabel, entityId);
	}

	public abstract String getEntityTypeId();

	public abstract String getEntityTypeLabel();

	public abstract String getEntityId();
}
