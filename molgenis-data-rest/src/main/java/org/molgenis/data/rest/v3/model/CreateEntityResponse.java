package org.molgenis.data.rest.v3.model;

import com.google.auto.value.AutoValue;

import java.net.URI;

@AutoValue
public abstract class CreateEntityResponse
{
	public abstract URI getLocation();

	public static Builder builder()
	{
		return new AutoValue_CreateEntityResponse.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setLocation(URI newLocation);

		public abstract CreateEntityResponse build();
	}
}
