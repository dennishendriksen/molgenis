package org.molgenis.settings.controller;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.List;

@AutoValue
@SuppressWarnings("squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class SettingsTypesResponse
{
	public abstract List<String> getEntityTypeIds();

	public static SettingsTypesResponse create(List<String> settingsIds)
	{
		return new AutoValue_SettingsResponse(ImmutableList.copyOf(settingsIds));
	}
}
