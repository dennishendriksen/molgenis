package org.molgenis.data.rest.v3.model;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.List;

@AutoValue
public abstract class ReadEntitiesResponse
{
	public abstract URI getHref();

	@Nullable
	public abstract ResponseEntity getMeta();

	@Nullable
	public abstract List<ResponseEntity> getItems();

	public static Builder builder()
	{
		return new AutoValue_ReadEntitiesResponse.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setHref(URI newHref);

		public abstract Builder setMeta(ResponseEntity newMeta);

		public abstract Builder setItems(List<ResponseEntity> newItems);

		public abstract ReadEntitiesResponse build();
	}
}
