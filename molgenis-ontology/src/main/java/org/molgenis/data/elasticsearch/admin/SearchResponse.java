package org.molgenis.data.elasticsearch.admin;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import org.molgenis.gson.AutoGson;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_SearchResponse.class)
public abstract class SearchResponse
{
	static SearchResponse create(List<SearchHit> hits, long total)
	{
		return new AutoValue_SearchResponse(ImmutableList.copyOf(hits), total);
	}

	public abstract List<SearchHit> hits();

	public abstract long total();
}
