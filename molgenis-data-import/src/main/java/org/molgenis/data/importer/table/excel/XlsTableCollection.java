package org.molgenis.data.importer.table.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.molgenis.data.importer.table.Table;
import org.molgenis.data.importer.table.TableCollection;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

public class XlsTableCollection implements TableCollection
{
	private final Path path;

	public XlsTableCollection(Path path)
	{
		this.path = requireNonNull(path);
	}

	@Override
	public Stream<Table> getTableStream()
	{
		NPOIFSFileSystem npoifsFileSystem;
		try
		{
			npoifsFileSystem = new NPOIFSFileSystem(Files.newInputStream(path));
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}

		Workbook workbook;
		try
		{
			workbook = new HSSFWorkbook(npoifsFileSystem.getRoot(), true);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}

		Iterator<Table> excelTableIterator = new ExcelTableIterator(workbook);
		Runnable runnable = () ->
		{
			try
			{
				npoifsFileSystem.close();
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		};
		return stream(spliteratorUnknownSize(excelTableIterator, ORDERED), false).onClose(runnable);
	}
}
