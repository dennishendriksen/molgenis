package org.molgenis.data.importer.table.excel;

import org.molgenis.data.importer.table.Cell;
import org.molgenis.data.importer.table.Row;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

class ExcelRow implements Row
{
	private final org.apache.poi.ss.usermodel.Row row;

	ExcelRow(org.apache.poi.ss.usermodel.Row row)
	{
		this.row = requireNonNull(row);
	}

	@Override
	public Stream<Cell> getValues()
	{
		return stream(spliteratorUnknownSize(row.cellIterator(), ORDERED), false).map(this::toCell);
	}

	@Override
	public long getIndex()
	{
		return row.getRowNum();
	}

	private Cell toCell(org.apache.poi.ss.usermodel.Cell cell)
	{
		return cell != null ? new ExcelCell(cell) : null;
	}
}
