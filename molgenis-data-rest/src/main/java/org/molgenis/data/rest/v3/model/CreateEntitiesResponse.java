package org.molgenis.data.rest.v3.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import java.net.URI;
import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CreateEntitiesResponse.class)
public abstract class CreateEntitiesResponse
{
	public abstract List<URI> getLocations();

	public static Builder builder()
	{
		return new AutoValue_CreateEntitiesResponse.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setLocations(List<URI> newLocations);

		public abstract CreateEntitiesResponse build();
	}
}
