package org.molgenis.data.importer.table.excel;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

public class XlsxTableCollection implements TableCollection
{
	private final Path path;

	public XlsxTableCollection(Path path)
	{
		this.path = requireNonNull(path);
	}

	@Override
	public Stream<Table> getTableStream()
	{
		OPCPackage opcPackage;
		try
		{
			opcPackage = OPCPackage.open(Files.newInputStream(path));
		}
		catch (InvalidFormatException e)
		{
			throw new UncheckedIOException(new IOException(e));
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}

		Workbook workbook;
		try
		{
			workbook = new XSSFWorkbook(opcPackage);
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
				opcPackage.close();
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		};
		return stream(spliteratorUnknownSize(excelTableIterator, ORDERED), false).onClose(runnable);
	}
}
