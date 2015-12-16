package org.molgenis.data.meta;

import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class PackageMetaData extends DefaultEntityMetaData
{
	public static final PackageMetaData INSTANCE = new PackageMetaData();

	public static final String ENTITY_NAME = "packages";

	public static final AttributeMetaData FULL_NAME;
	public static final AttributeMetaData SIMPLE_NAME;
	public static final AttributeMetaData DESCRIPTION;
	public static final AttributeMetaData PARENT;
	public static final AttributeMetaData TAGS;

	static
	{
		FULL_NAME = attribute("fullName").setIdAttribute(true).setNillable(false).setLabelAttribute(true);
		SIMPLE_NAME = attribute("name");
		DESCRIPTION = attribute("description").setDataType(TEXT);
		PARENT = attribute("parent").setDataType(XREF).setRefEntity(INSTANCE);
		TAGS = attribute("tags").setDataType(MREF).setRefEntity(TagMetaData.INSTANCE);
	}

	private PackageMetaData()
	{
		super(ENTITY_NAME);

		addAttributeMetaData(FULL_NAME).addAttributeMetaData(SIMPLE_NAME).addAttributeMetaData(DESCRIPTION)
				.addAttributeMetaData(PARENT).addAttributeMetaData(TAGS);
	}
}
