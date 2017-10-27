package org.molgenis.data.importer.table.zip;

import org.molgenis.data.importer.table.Table;
import org.molgenis.data.importer.table.TableCollection;
import org.molgenis.data.importer.table.TableCollectionFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

public class ArchiveTableCollection implements TableCollection
{
	private final Path path;
	private final TableCollectionFactory tableCollectionFactory;

	public ArchiveTableCollection(Path path, TableCollectionFactory tableCollectionFactory)
	{
		this.path = requireNonNull(path);
		this.tableCollectionFactory = requireNonNull(tableCollectionFactory);
	}

	@Override
	public Stream<Table> getTableStream()
	{
		FileSystem fileSystem;
		try
		{
			fileSystem = FileSystems.newFileSystem(path, null);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
		return getTableStream(fileSystem);
	}

	private Stream<Table> getTableStream(FileSystem fileSystem)
	{
		return getTableFiles(fileSystem).flatMap(this::getTables);
	}

	private Stream<Table> getTables(Path path)
	{
		return tableCollectionFactory.createTableCollection(path).getTableStream();
	}

	private static Stream<Path> getTableFiles(FileSystem fileSystem)
	{
		return stream(fileSystem.getRootDirectories().spliterator(), false).flatMap(rootPath ->
		{
			try
			{
				return Files.walk(rootPath);
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}).filter(Files::isRegularFile);
	}
}
