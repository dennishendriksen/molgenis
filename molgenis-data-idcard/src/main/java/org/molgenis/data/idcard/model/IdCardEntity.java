package org.molgenis.data.idcard.model;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;

public abstract class IdCardEntity extends DefaultEntity
{
	private static final long serialVersionUID = 1L;
	public static final String ORGANIZATION_ID = "OrganizationID";
        public static final String IDCARD_URL = "idcardurl"; // use
	public static final String NAME_OF_HOST_INSTITUTION = "name_of_host_institution"; // use
	public static final String COUNTRY = "country"; // use
	public static final String CITY = "city"; // use
	public static final String NAME = "name"; // use

	public IdCardEntity(final EntityMetaData entityMetaData, final DataService dataService)
	{
		super(entityMetaData, dataService);
	}

}
