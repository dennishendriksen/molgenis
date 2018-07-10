package org.molgenis.data.csv;

import org.molgenis.data.Cell;
import org.molgenis.data.Row;

import java.util.Arrays;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

class CsvRow implements Row
{
	private final String[] line;

	CsvRow(String[] line)
	{
		this.line = requireNonNull(line);
	}

	@Override
	public void forEach(Consumer<Cell> action)
	{
		for (String value : line)
		{
			action.accept(new CsvCell(value));
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CsvRow csvRow = (CsvRow) o;
		return Arrays.equals(line, csvRow.line);
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(line);
	}

	@Override
	public String toString()
	{
		return "CsvRow{" + "line=" + Arrays.toString(line) + '}';
	}
}
