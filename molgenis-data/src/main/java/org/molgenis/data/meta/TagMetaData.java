package org.molgenis.data.meta;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class TagMetaData extends DefaultEntityMetaData
{
	public static final TagMetaData INSTANCE = new TagMetaData();

	public static final String ENTITY_NAME = "tags";

	public static final AttributeMetaData IDENTIFIER;
	public static final AttributeMetaData OBJECT_IRI;
	public static final AttributeMetaData LABEL;
	public static final AttributeMetaData RELATION_IRI;
	public static final AttributeMetaData RELATION_LABEL;
	public static final AttributeMetaData CODE_SYSTEM;

	static
	{
		IDENTIFIER = attribute("identifier").setIdAttribute(true).setNillable(false);
		OBJECT_IRI = attribute("objectIRI").setLookupAttribute(true).setDataType(MolgenisFieldTypes.TEXT);
		LABEL = attribute("label").setNillable(false).setLookupAttribute(true).setLabelAttribute(true);
		RELATION_IRI = attribute("relationIRI").setNillable(false);
		RELATION_LABEL = attribute("relationLabel").setNillable(false);
		CODE_SYSTEM = attribute("codeSystem");
	}

	private TagMetaData()
	{
		super(ENTITY_NAME);
		addAttributeMetaData(IDENTIFIER).addAttributeMetaData(OBJECT_IRI).addAttributeMetaData(LABEL)
				.addAttributeMetaData(RELATION_IRI).addAttributeMetaData(RELATION_LABEL)
				.addAttributeMetaData(CODE_SYSTEM);
	}
}
