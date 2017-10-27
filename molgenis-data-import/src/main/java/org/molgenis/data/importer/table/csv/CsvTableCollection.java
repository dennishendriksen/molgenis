package org.molgenis.data.importer.table.csv;

import org.molgenis.data.importer.table.Table;
import org.molgenis.data.importer.table.TableCollection;
import org.molgenis.file.model.FileMeta;

import java.nio.file.Path;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class CsvTableCollection implements TableCollection
{
	private final Path path;
	private final FileMeta fileMeta;

	public CsvTableCollection(Path path)
	{
		this(path, null);
	}

	public CsvTableCollection(Path path, FileMeta fileMeta)
	{
		this.path = requireNonNull(path);
		this.fileMeta = fileMeta;
	}

	@Override
	public Stream<Table> getTableStream()
	{
		return Stream.of(new CsvTable(path, fileMeta));
	}
}
