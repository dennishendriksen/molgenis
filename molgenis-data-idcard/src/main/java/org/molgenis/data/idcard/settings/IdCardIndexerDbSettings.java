package org.molgenis.data.idcard.settings;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.EMAIL;
import static org.molgenis.MolgenisFieldTypes.LONG;
import static org.molgenis.MolgenisFieldTypes.STRING;

import org.molgenis.data.idcard.indexer.IdCardIndexerController;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.molgenis.fieldtypes.StringField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IdCardIndexerDbSettings extends DefaultSettingsEntity implements IdCardIndexerSettings {
    private static final Logger LOG = LoggerFactory.getLogger(IdCardIndexerDbSettings.class);

    private static final long serialVersionUID = 1L;

    private static final String ID = IdCardIndexerController.ID;

    public IdCardIndexerDbSettings() {
        super(ID);
    }

    @Component
    private static class Meta extends DefaultSettingsEntityMetaData {

        private static final String API_BASE_URI = "apiBaseUri";
        private static final String API_TIMEOUT = "apiTimeout";
        private static final String ORGANISATION_RESOURCE = "organisationResource";
        private static final String BIOBANK_COLLECTIONS_RESOURCE = "biobankCollResource";
        private static final String BIOBANK_COLLECTIONS_SELECTION_RESOURCE = "biobankCollSelResource";
        private static final String REGISTRY_COLLECTIONS_RESOURCE = "registryCollResource";
        private static final String REGISTRY_COLLECTIONS_SELECTION_RESOURCE = "registryCollSelResource";
        private static final String INDEXING_ENABLED = "biobankIndexingEnabled";
        private static final String INDEXING_TIMEOUT = "biobankIndexingTimeout";
        private static final String NOTIFICATION_EMAIL = "notificationEmail";

        private static final String INDEXING_FREQUENCY = "biobankIndexingFrequency";
        private static final String DEFAULT_API_BASE_URI = "http://catalogue.rd-connect.eu/api/jsonws/BiBBoxCommonServices-portlet.logapi";
        private static final long DEFAULT_API_TIMEOUT = 5000l;
        private static final String DEFAULT_ORGANISATION_RESOURCE = "regbb/organization-id";
        private static final String DEFAULT_BIOBANK_COLLECTIONS_RESOURCE = "bbs";
        private static final String DEFAULT_BIOBANK_COLLECTIONS_SELECTION_RESOURCE = DEFAULT_BIOBANK_COLLECTIONS_RESOURCE
                + "/data";
        private static final String DEFAULT_REGISTRY_COLLECTIONS_RESOURCE = "regs";
        private static final String DEFAULT_REGISTRY_COLLECTIONS_SELECTION_RESOURCE = DEFAULT_REGISTRY_COLLECTIONS_RESOURCE
                + "/data";
        private static final boolean DEFAULT_INDEXING_ENABLED = false;
        private static final long DEFAULT_INDEXING_TIMEOUT = 60000l;
        private static final String DEFAULT_INDEXING_FREQUENCY = "0 4 * * * ?";
        private static final String DEFAULT_NOTIFICATION_EMAIL = "molgenis+idcard@gmail.com";

        public Meta() {
            super(ID);
            setLabel("ID-Card indexer settings");
            // Remote host
            addAttribute(API_BASE_URI).setDataType(STRING).setLabel("API base URI")
                    .setDefaultValue(DEFAULT_API_BASE_URI);
            addAttribute(API_TIMEOUT).setDataType(LONG).setLabel("API timeout")
                    .setDefaultValue(Long.toString(DEFAULT_API_TIMEOUT));
            // Organisations
            addAttribute(ORGANISATION_RESOURCE).setDataType(STRING).setLabel("Organisation resource")
                    .setDefaultValue(DEFAULT_ORGANISATION_RESOURCE);
            // Biobanks
            addAttribute(BIOBANK_COLLECTIONS_RESOURCE).setDataType(STRING).setLabel("Biobank collection resource")
                    .setDefaultValue(DEFAULT_BIOBANK_COLLECTIONS_RESOURCE);
            addAttribute(BIOBANK_COLLECTIONS_SELECTION_RESOURCE).setDataType(STRING)
                    .setLabel("Biobank collection filtered resource")
                    .setDefaultValue(DEFAULT_BIOBANK_COLLECTIONS_SELECTION_RESOURCE);
            // Registries
            addAttribute(REGISTRY_COLLECTIONS_RESOURCE).setDataType(STRING).setLabel("Registry collection resource")
                    .setDefaultValue(DEFAULT_REGISTRY_COLLECTIONS_RESOURCE);
            addAttribute(REGISTRY_COLLECTIONS_SELECTION_RESOURCE).setDataType(STRING)
                    .setLabel("Registry collection filtered resource")
                    .setDefaultValue(DEFAULT_REGISTRY_COLLECTIONS_SELECTION_RESOURCE);
            // Indexing
            addAttribute(INDEXING_ENABLED).setDataType(BOOL).setLabel("Biobank indexing enabled")
                    .setDefaultValue(Boolean.toString(DEFAULT_INDEXING_ENABLED)).setNillable(false);
            addAttribute(INDEXING_TIMEOUT).setDataType(LONG).setLabel("Biobank indexing timeout")
                    .setDefaultValue(Long.toString(DEFAULT_INDEXING_TIMEOUT)).setNillable(false);
            addAttribute(INDEXING_FREQUENCY).setDataType(STRING).setLabel("Biobank indexing frequency")
                    .setDescription("Cron expression (e.g. 0 4 * * * ?)")
                    .setDefaultValue(DEFAULT_INDEXING_FREQUENCY).setNillable(false)
                    .setVisibleExpression("$('" + INDEXING_ENABLED + "').eq(true).value()")
                    .setValidationExpression(
                            "$('" + INDEXING_FREQUENCY + "').matches(" + StringField.CRON_REGEX + ").value()");
            addAttribute(NOTIFICATION_EMAIL).setDataType(EMAIL).setLabel("Notification email")
                    .setDescription("email address used for index failure notifications")
                    .setDefaultValue(DEFAULT_NOTIFICATION_EMAIL);
        }
    }

    @Override
    public String getApiBaseUri() {
        return getString(Meta.API_BASE_URI);
    }

    @Override
    public void setApiBaseUri(String apiBaseUri) {
        set(Meta.API_BASE_URI, apiBaseUri);
    }

    @Override
    public long getApiTimeout() {
        return getLong(Meta.API_TIMEOUT);
    }

    @Override
    public void setApiTimeout(long timeout) {
        set(Meta.API_TIMEOUT, timeout);
    }

    @Override
    public String getBiobankCollectionResource() {
        LOG.debug("biobank collection resource");
        return getString(Meta.BIOBANK_COLLECTIONS_RESOURCE);
    }

    @Override
    public void setBiobankCollectionResource(String biobankCollectionResource) {
        set(Meta.BIOBANK_COLLECTIONS_RESOURCE, biobankCollectionResource);
    }

    @Override
    public String getBiobankCollectionSelectionResource() {
        LOG.debug("biobank collection selection resource");
        return getString(Meta.BIOBANK_COLLECTIONS_SELECTION_RESOURCE);
    }

    @Override
    public void setBiobankCollectionSelectionResource(String biobankCollectionSelectionResource) {
        set(Meta.BIOBANK_COLLECTIONS_SELECTION_RESOURCE, biobankCollectionSelectionResource);
    }

    @Override
    public String getRegistryCollectionResource() {
        LOG.debug("registry collection resource");
        return getString(Meta.REGISTRY_COLLECTIONS_RESOURCE);
    }

    @Override
    public void setRegistryCollectionResource(String registryCollectionResource) {
        set(Meta.REGISTRY_COLLECTIONS_RESOURCE, registryCollectionResource);
    }

    @Override
    public String getRegistryCollectionSelectionResource() {
        LOG.debug("registry collection selection resource");
        return getString(Meta.REGISTRY_COLLECTIONS_SELECTION_RESOURCE);
    }

    @Override
    public void setRegistryCollectionSelectionResource(String registryCollectionSelectionResource) {
        set(Meta.REGISTRY_COLLECTIONS_SELECTION_RESOURCE, registryCollectionSelectionResource);
    }

    @Override
    public String getOrganisationResource() {
        return getString(Meta.ORGANISATION_RESOURCE);
    }

    @Override
    public void setOrganisationResource(String registryResource) {
        set(Meta.ORGANISATION_RESOURCE, registryResource);
    }

    @Override
    public boolean getIndexingEnabled() {
        Boolean enableBiobankIndexing = getBoolean(Meta.INDEXING_ENABLED);
        return enableBiobankIndexing != null ? enableBiobankIndexing : false;
    }

    @Override
    public void setIndexingEnabled(boolean biobankIndexing) {
        set(Meta.INDEXING_ENABLED, biobankIndexing);
    }

    @Override
    public String getIndexingFrequency() {
        return getString(Meta.INDEXING_FREQUENCY);
    }

    @Override
    public void setIndexingFrequency(String cronExpression) {
        set(Meta.INDEXING_FREQUENCY, cronExpression);
    }

    @Override
    public String getNotificationEmail() {
        return getString(Meta.NOTIFICATION_EMAIL);
    }

    @Override
    public void setNotificationEmail(String notificationEmail) {
        set(Meta.NOTIFICATION_EMAIL, notificationEmail);
    }

    @Override
    public void setIndexRebuildTimeout(long timeout) {
        set(Meta.INDEXING_TIMEOUT, timeout);
    }

    @Override
    public long getIndexRebuildTimeout() {
        return getLong(Meta.INDEXING_TIMEOUT);
    }
}
