package org.molgenis.data.csv;

import java.nio.file.Path;

public class CsvTableFactory
{
	private CsvTableFactory()
	{
	}

	public static CsvTable create(Path path)
	{
		char separator = CsvUtils.determineSeparator(path);
		return new CsvTable(path, separator);
	}
}
