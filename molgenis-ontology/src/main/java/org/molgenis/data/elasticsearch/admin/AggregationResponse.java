package org.molgenis.data.elasticsearch.admin;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AggregationResponse.class)
public abstract class AggregationResponse
{
	static AggregationResponse create(List<AggregationBucket> buckets)
	{
		return new AutoValue_AggregationResponse(buckets);
	}

	public abstract List<AggregationBucket> buckets();
}
