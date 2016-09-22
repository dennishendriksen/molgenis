package org.molgenis.data.idcard.model;

import static org.molgenis.MolgenisFieldTypes.HYPERLINK;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LOOKUP;

import org.molgenis.data.support.DefaultEntityMetaData;

public abstract class IdCardEntityMetaData<E extends IdCardEntity> extends DefaultEntityMetaData {

    public IdCardEntityMetaData(final String entityName, final Class<? extends IdCardEntity> clazz) {
        super(entityName, clazz);
        setBackend(getBackendName());

        addAttribute(IdCardEntity.NAME, ROLE_LABEL, ROLE_LOOKUP).setLabel("Name");
        addAttribute(IdCardEntity.NAME_OF_HOST_INSTITUTION).setLabel("Host institution");
        addAttribute(IdCardEntity.CITY).setLabel("City");
        addAttribute(IdCardEntity.COUNTRY).setLabel("Country");
        addAttribute(IdCardEntity.IDCARD_URL).setLabel("ID Card").setDataType(HYPERLINK);
        addAttribute(IdCardEntity.ORGANIZATION_ID, ROLE_ID).setLabel("OrganizationID").setVisible(false);
    }

    abstract protected String getBackendName();
}
