package org.molgenis.data.elasticsearch.admin;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AggregationBucket.class)
public abstract class AggregationBucket
{
	static AggregationBucket create(String id, String label, long count)
	{
		return new AutoValue_AggregationBucket(id, label, count);
	}

	public abstract String id();

	public abstract String label();

	public abstract long count();
}
