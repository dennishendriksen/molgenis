package org.molgenis.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
@SuppressWarnings("squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class AttributeSearchHits
{
	public abstract List<AttributeSearchHit> getHits();

	public static AttributeSearchHits create(List<AttributeSearchHit> attributeSearchHits)
	{
		return new AutoValue_AttributeSearchHits(attributeSearchHits);
	}
}
