package org.molgenis.data.meta;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.SCRIPT;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.TEXT;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.LongField;

public class AttributeMetaDataMetaData extends DefaultEntityMetaData
{
	public static final AttributeMetaDataMetaData INSTANCE = new AttributeMetaDataMetaData();

	public static final String ENTITY_NAME = "attributes";

	public static final AttributeMetaData IDENTIFIER;
	public static final AttributeMetaData NAME;
	public static final AttributeMetaData DATA_TYPE;
	public static final AttributeMetaData REF_ENTITY;
	public static final AttributeMetaData EXPRESSION;
	public static final AttributeMetaData NILLABLE;
	public static final AttributeMetaData AUTO;
	public static final AttributeMetaData ID_ATTRIBUTE;
	public static final AttributeMetaData LOOKUP_ATTRIBUTE;
	public static final AttributeMetaData VISIBLE;
	public static final AttributeMetaData LABEL;
	public static final AttributeMetaData DESCRIPTION;
	public static final AttributeMetaData AGGREGATEABLE;
	public static final AttributeMetaData ENUM_OPTIONS;
	public static final AttributeMetaData RANGE_MIN;
	public static final AttributeMetaData RANGE_MAX;
	public static final AttributeMetaData LABEL_ATTRIBUTE;
	public static final AttributeMetaData READ_ONLY;
	public static final AttributeMetaData UNIQUE;
	public static final AttributeMetaData PARTS;
	public static final AttributeMetaData TAGS;
	public static final AttributeMetaData VISIBLE_EXPRESSION;
	public static final AttributeMetaData VALIDATION_EXPRESSION;
	public static final AttributeMetaData DEFAULT_VALUE;

	static
	{
		IDENTIFIER = attribute("identifier").setIdAttribute(true).setNillable(false).setDataType(STRING)
				.setVisible(false);
		NAME = attribute("name").setNillable(false).setLabelAttribute(true).setLookupAttribute(true);
		DATA_TYPE = attribute("dataType");
		PARTS = attribute("parts").setDataType(MREF).setRefEntity(INSTANCE);
		REF_ENTITY = attribute("refEntity");
		EXPRESSION = attribute("expression").setNillable(true);
		NILLABLE = attribute("nillable").setDataType(BOOL).setNillable(false);
		AUTO = attribute("auto").setDataType(BOOL).setNillable(false);
		ID_ATTRIBUTE = attribute("idAttribute").setDataType(BOOL).setNillable(false);
		LOOKUP_ATTRIBUTE = attribute("lookupAttribute").setDataType(BOOL).setNillable(false);
		VISIBLE = attribute("visible").setDataType(BOOL).setNillable(false);
		LABEL = attribute("label").setLookupAttribute(true);
		DESCRIPTION = attribute("description").setDataType(TEXT);
		AGGREGATEABLE = attribute("aggregateable").setDataType(BOOL).setNillable(false);
		ENUM_OPTIONS = attribute("enumOptions").setDataType(TEXT);
		RANGE_MIN = attribute("rangeMin").setDataType(new LongField());
		RANGE_MAX = attribute("rangeMax").setDataType(new LongField());
		LABEL_ATTRIBUTE = attribute("labelAttribute").setDataType(BOOL).setNillable(false);
		READ_ONLY = attribute("readOnly").setDataType(BOOL).setNillable(false);
		UNIQUE = attribute("unique").setDataType(BOOL).setNillable(false);
		TAGS = attribute("tags").setDataType(MREF).setRefEntity(TagMetaData.INSTANCE);
		VISIBLE_EXPRESSION = attribute("visibleExpression").setDataType(SCRIPT).setNillable(true);
		VALIDATION_EXPRESSION = attribute("validationExpression").setDataType(SCRIPT).setNillable(true);
		DEFAULT_VALUE = attribute("defaultValue").setDataType(TEXT).setNillable(true);
	}

	private AttributeMetaDataMetaData()
	{
		super(ENTITY_NAME);

		addAttributeMetaData(IDENTIFIER).addAttributeMetaData(NAME).addAttributeMetaData(DATA_TYPE)
				.addAttributeMetaData(PARTS).addAttributeMetaData(REF_ENTITY).addAttributeMetaData(EXPRESSION)
				.addAttributeMetaData(NILLABLE).addAttributeMetaData(AUTO).addAttributeMetaData(ID_ATTRIBUTE)
				.addAttributeMetaData(LOOKUP_ATTRIBUTE).addAttributeMetaData(VISIBLE).addAttributeMetaData(LABEL)
				.addAttributeMetaData(DESCRIPTION).addAttributeMetaData(AGGREGATEABLE)
				.addAttributeMetaData(ENUM_OPTIONS).addAttributeMetaData(RANGE_MIN).addAttributeMetaData(RANGE_MAX)
				.addAttributeMetaData(LABEL_ATTRIBUTE).addAttributeMetaData(READ_ONLY).addAttributeMetaData(UNIQUE)
				.addAttributeMetaData(TAGS).addAttributeMetaData(VISIBLE_EXPRESSION)
				.addAttributeMetaData(VALIDATION_EXPRESSION).addAttributeMetaData(DEFAULT_VALUE);
	}
}
