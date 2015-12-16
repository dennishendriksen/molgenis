package org.molgenis.data.meta;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class EntityMetaDataMetaData extends DefaultEntityMetaData
{
	public static final EntityMetaDataMetaData INSTANCE = new EntityMetaDataMetaData();

	public static final String ENTITY_NAME = "entities";

	public static final AttributeMetaData SIMPLE_NAME;
	public static final AttributeMetaData BACKEND;
	public static final AttributeMetaData FULL_NAME;
	public static final AttributeMetaData ID_ATTRIBUTE;
	public static final AttributeMetaData LABEL_ATTRIBUTE;
	public static final AttributeMetaData ABSTRACT;
	public static final AttributeMetaData LABEL;
	public static final AttributeMetaData EXTENDS;
	public static final AttributeMetaData DESCRIPTION;
	public static final AttributeMetaData PACKAGE;
	public static final AttributeMetaData TAGS;
	public static final AttributeMetaData ATTRIBUTES;

	static
	{
		FULL_NAME = attribute("fullName").setIdAttribute(true).setUnique(true).setNillable(false);
		SIMPLE_NAME = attribute("simpleName").setNillable(false);
		BACKEND = attribute("backend");
		PACKAGE = attribute("package").setDataType(XREF).setRefEntity(PackageRepository.META_DATA);
		ID_ATTRIBUTE = attribute("idAttribute");
		LABEL_ATTRIBUTE = attribute("labelAttribute");
		ABSTRACT = attribute("abstract").setDataType(BOOL);
		LABEL = attribute("label").setLabelAttribute(true).setLookupAttribute(true);
		EXTENDS = attribute("extends").setDataType(XREF).setRefEntity(INSTANCE);
		DESCRIPTION = attribute("description").setDataType(TEXT).setLookupAttribute(false);
		TAGS = attribute("tags").setDataType(MREF).setRefEntity(TagMetaData.INSTANCE);
		ATTRIBUTES = attribute("attributes").setDataType(MREF).setRefEntity(AttributeMetaDataMetaData.INSTANCE);
	}

	private EntityMetaDataMetaData()
	{
		super(ENTITY_NAME);
		addAttributeMetaData(FULL_NAME).addAttributeMetaData(SIMPLE_NAME).addAttributeMetaData(BACKEND)
				.addAttributeMetaData(PACKAGE).addAttributeMetaData(ID_ATTRIBUTE).addAttributeMetaData(LABEL_ATTRIBUTE)
				.addAttributeMetaData(ABSTRACT).addAttributeMetaData(LABEL).addAttributeMetaData(EXTENDS)
				.addAttributeMetaData(DESCRIPTION).addAttributeMetaData(TAGS).addAttributeMetaData(ATTRIBUTES);
	}
}
