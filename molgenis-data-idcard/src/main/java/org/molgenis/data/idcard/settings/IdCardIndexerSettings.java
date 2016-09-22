package org.molgenis.data.idcard.settings;

import org.molgenis.data.settings.SettingsEntityListener;

public interface IdCardIndexerSettings
{
	String getApiBaseUri();

	void setApiBaseUri(String idCardApiBaseUri);

	long getApiTimeout();

	void setApiTimeout(long timeout);

	String getBiobankCollectionResource();

	void setBiobankCollectionResource(String biobankCollectionResource);

	String getBiobankCollectionSelectionResource();

	void setBiobankCollectionSelectionResource(String biobankCollectionSelectionResource);

	String getOrganisationResource();

	void setOrganisationResource(String registryResource);

	String getRegistryCollectionResource();

	void setRegistryCollectionResource(String registryCollectionResource);

	String getRegistryCollectionSelectionResource();

	void setRegistryCollectionSelectionResource(String registryCollectionSelectionResource);

	boolean getIndexingEnabled();

	void setIndexingEnabled(boolean biobankIndexing);

	String getIndexingFrequency();

	void setIndexingFrequency(String cronExpression);

	void addListener(SettingsEntityListener settingsEntityListener);

	void removeListener(SettingsEntityListener settingsEntityListener);

	String getNotificationEmail();

	void setNotificationEmail(String notificationEmail);

	long getIndexRebuildTimeout();

	void setIndexRebuildTimeout(long timeout);
}