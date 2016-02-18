package org.molgenis.data.meta;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LOOKUP;

import org.molgenis.data.support.SystemEntityMetaData;

public class EntityMetaDataMetaData extends SystemEntityMetaData
{
	public static final String ENTITY_NAME = "entities";
	public static final String SIMPLE_NAME = "simpleName";
	public static final String BACKEND = "backend";
	public static final String FULL_NAME = "fullName";
	public static final String ID_ATTRIBUTE = "idAttribute";
	public static final String LABEL_ATTRIBUTE = "labelAttribute";
	public static final String LOOKUP_ATTRIBUTES = "lookupAttributes";
	public static final String ABSTRACT = "abstract";
	public static final String LABEL = "label";
	public static final String EXTENDS = "extends";
	public static final String DESCRIPTION = "description";
	public static final String PACKAGE = "package";
	public static final String TAGS = "tags";
	public static final String ATTRIBUTES = "attributes";
	public static final String SYSTEM = "system";

	public static final EntityMetaDataMetaData INSTANCE = new EntityMetaDataMetaData();

	private EntityMetaDataMetaData()
	{
		super(ENTITY_NAME);
		setLabel("Entity");
		addAttribute(FULL_NAME, ROLE_ID).setLabel("Fully qualified name");
		addAttribute(SIMPLE_NAME, ROLE_LABEL).setLabel("Name").setNillable(false).setReadOnly(true);
		addAttribute(BACKEND).setLabel("Backend").setReadOnly(true);
		addAttribute(PACKAGE).setLabel("Package").setDataType(XREF).setRefEntity(PackageRepository.META_DATA)
				.setReadOnly(true);
		addAttribute(ID_ATTRIBUTE).setLabel("ID attribute").setDataType(XREF)
				.setRefEntity(AttributeMetaDataMetaData.INSTANCE).setReadOnly(true);
		addAttribute(LABEL_ATTRIBUTE).setLabel("Label attribute").setDataType(XREF)
				.setRefEntity(AttributeMetaDataMetaData.INSTANCE).setReadOnly(true);
		addAttribute(LOOKUP_ATTRIBUTES).setLabel("Lookup attributes").setDataType(MREF)
				.setRefEntity(AttributeMetaDataMetaData.INSTANCE).setReadOnly(true);
		addAttribute(ABSTRACT).setLabel("Abstract").setDataType(BOOL).setReadOnly(true);
		addAttribute(LABEL, ROLE_LOOKUP).setLabel("Label");
		addAttribute(EXTENDS).setLabel("Extends").setDataType(XREF).setRefEntity(this).setReadOnly(true);
		addAttribute(DESCRIPTION, ROLE_LOOKUP).setLabel("Description").setDataType(TEXT);
		addAttribute(TAGS).setDataType(MREF).setLabel("Tags").setRefEntity(TagMetaData.INSTANCE);
		addAttribute(ATTRIBUTES).setLabel("Attributes").setDataType(MREF)
				.setRefEntity(AttributeMetaDataMetaData.INSTANCE);
		addAttribute(SYSTEM).setLabel("System").setDataType(BOOL).setNillable(false).setReadOnly(true);
	}
}
