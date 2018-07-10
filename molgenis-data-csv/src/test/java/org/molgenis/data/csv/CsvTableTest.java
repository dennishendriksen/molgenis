package org.molgenis.data.csv;

import org.molgenis.data.Row;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.molgenis.util.ResourceUtils.getFile;
import static org.testng.Assert.assertEquals;

public class CsvTableTest
{
	@Test
	public void testForEach()
	{
		Path path = getFile(getClass(), "/test.csv").toPath();
		CsvTable csvTable = new CsvTable(path, ',');
		List<Row> rows = new ArrayList<>();
		csvTable.forEach(rows::add);
		assertEquals(rows,
				asList(new CsvRow(new String[] { "col1", "col2" }), new CsvRow(new String[] { "val1", "val2" })));
	}
}