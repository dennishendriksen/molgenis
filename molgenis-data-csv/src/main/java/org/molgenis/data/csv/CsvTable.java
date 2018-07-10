package org.molgenis.data.csv;

import au.com.bytecode.opencsv.CSVReader;
import org.molgenis.data.Row;
import org.molgenis.data.Table;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newInputStream;
import static java.util.Objects.requireNonNull;

public class CsvTable implements Table
{
	private final Path path;
	private final char separator;

	public CsvTable(Path path, char separator)
	{
		this.path = requireNonNull(path);
		this.separator = separator;
	}

	@Override
	public void forEach(Consumer<Row> action)
	{
		try (CSVReader csvReader = new CSVReader(new InputStreamReader(newInputStream(path), UTF_8), separator))
		{
			String[] line;
			while ((line = csvReader.readNext()) != null)
			{
				action.accept(new CsvRow(line));
			}
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
