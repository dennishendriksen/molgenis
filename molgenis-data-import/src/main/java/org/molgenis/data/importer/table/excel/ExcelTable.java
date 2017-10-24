package org.molgenis.data.importer.table.excel;

import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.importer.table.Row;
import org.molgenis.data.importer.table.Table;

import java.util.Iterator;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

class ExcelTable implements Table
{
	private final Sheet sheet;

	ExcelTable(Sheet sheet)
	{
		this.sheet = requireNonNull(sheet);
	}

	@Override
	public String getName()
	{
		return sheet.getSheetName();
	}

	@Override
	public Stream<Row> getRowStream()
	{
		Iterator<org.apache.poi.ss.usermodel.Row> sheetRowIterator = sheet.iterator();
		if (!sheetRowIterator.hasNext())
		{
			return Stream.empty();
		}

		return stream(spliteratorUnknownSize(sheetRowIterator, ORDERED), false).map(ExcelRow::new);
	}
}
