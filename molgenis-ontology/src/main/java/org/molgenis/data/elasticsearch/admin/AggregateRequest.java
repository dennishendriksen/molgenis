package org.molgenis.data.elasticsearch.admin;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

//TODO rename to AggregationRequest
@AutoValue
@AutoGson(autoValueClass = AutoValue_AggregateRequest.class)
public abstract class AggregateRequest
{
	static AggregateRequest create(String query)
	{
		return new AutoValue_AggregateRequest(query);
	}

	public abstract String query();
}
