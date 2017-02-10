package org.molgenis.data.elasticsearch.admin;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_SearchRequest.class)
public abstract class SearchRequest
{
	static SearchRequest create(String query, Long from, Long size)
	{
		return new AutoValue_SearchRequest(query, from, size);
	}

	public abstract String query();

	@Nullable
	public abstract Long from();

	@Nullable
	public abstract Long size();
}
