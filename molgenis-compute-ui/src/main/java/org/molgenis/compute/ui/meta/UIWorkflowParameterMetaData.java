package org.molgenis.compute.ui.meta;

import static org.molgenis.MolgenisFieldTypes.MREF;

import org.molgenis.data.support.DefaultEntityMetaData;

public class UIWorkflowParameterMetaData extends DefaultEntityMetaData
{
	public static final UIWorkflowParameterMetaData INSTANCE = new UIWorkflowParameterMetaData();
	private static final String ENTITY_NAME = "WorkflowParameter";

	public static final String IDENTIFIER = "identifier";
	public static final String KEY = "key_";
	public static final String VALUES = "values_";

	public UIWorkflowParameterMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);
		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setVisible(false);
		addAttribute(KEY).setNillable(false).setLabelAttribute(true);
		addAttribute(VALUES).setDataType(MREF).setRefEntity(UIWorkflowParameterValueMetaData.INSTANCE);
	}

}
