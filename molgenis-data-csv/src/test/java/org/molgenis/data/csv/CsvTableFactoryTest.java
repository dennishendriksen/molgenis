package org.molgenis.data.csv;

import org.molgenis.data.Row;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static org.molgenis.util.ResourceUtils.getFile;
import static org.testng.Assert.assertEquals;

public class CsvTableFactoryTest
{
	@DataProvider(name = "testCreateProvider")
	public static Iterator<Object[]> testCreateProvider()
	{
		return asList(new Object[] { "/separator-comma.csv" }, new Object[] { "/separator-tab.csv" },
				new Object[] { "/separator-semicolon.csv" }, new Object[] { "/separator-pipe.csv" }).iterator();
	}

	@Test(dataProvider = "testCreateProvider")
	public void testCreate(String resourceName)
	{
		Path path = getFile(getClass(), resourceName).toPath();
		CsvTable csvTable = CsvTableFactory.create(path);
		List<Row> rows = new ArrayList<>();
		csvTable.forEach(rows::add);
		assertEquals(rows,
				asList(new CsvRow(new String[] { "col1", "col2" }), new CsvRow(new String[] { "val1", "val2" })));
	}
}