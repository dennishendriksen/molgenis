package org.molgenis.data.importer.table.csv;

import com.google.common.base.Preconditions;
import org.molgenis.data.importer.table.Cell;
import org.molgenis.data.importer.table.CellType;

import static java.lang.String.format;

class CsvCell implements Cell
{
	private final String value;
	private final long rowIndex;
	private final long columnIndex;

	CsvCell(String value, long rowIndex, long columnIndex)
	{
		Preconditions.checkArgument(rowIndex >= 0);
		Preconditions.checkArgument(columnIndex >= 0);
		this.value = value;
		this.rowIndex = rowIndex;
		this.columnIndex = columnIndex;
	}

	@Override
	public CellType getType()
	{
		return CellType.STRING;
	}

	@Override
	public String getStringValue()
	{
		return value;
	}

	@Override
	public Double getNumericValue()
	{
		throw new IllegalArgumentException(
				format("Cell is of type '%s' instead of '%s'", CellType.NUMERIC, CellType.STRING));
	}

	@Override
	public Boolean getBooleanValue()
	{
		throw new IllegalArgumentException(
				format("Cell is of type '%s' instead of '%s'", CellType.BOOLEAN, CellType.STRING));
	}

	@Override
	public long getRowIndex()
	{
		return rowIndex;
	}

	@Override
	public long getColumnIndex()
	{
		return columnIndex;
	}
}
