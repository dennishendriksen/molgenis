package org.molgenis.data.rest.v3.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;
import java.net.URI;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ReadEntityResponse.class)
public abstract class ReadEntityResponse
{
	public abstract URI getHref();

	@Nullable
	public abstract ResponseEntity getMeta();

	@Nullable
	public abstract ResponseEntity getData();

	public static Builder builder()
	{
		return new AutoValue_ReadEntityResponse.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setHref(URI uri);

		public abstract Builder setMeta(ResponseEntity newMeta);

		public abstract Builder setData(ResponseEntity newData);

		public abstract ReadEntityResponse build();
	}
}
