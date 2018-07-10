package org.molgenis.data;

import java.util.HashMap;
import java.util.Map;

public class TableUtils
{
	private TableUtils()
	{
	}

	public static Map<String, Integer> getValueIndexMap(Row row)
	{

		Map<String, Integer> attributeIndex = new HashMap<>();
		table.getHeaderRow().forEachCell(cell -> attributeIndex.put(cell.getValueAsString(), cell.getColumnIndex()));
	}
}
