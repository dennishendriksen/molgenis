package org.molgenis.data.importer.table.csv;

import com.google.common.base.Preconditions;
import org.molgenis.data.importer.table.Cell;
import org.molgenis.data.importer.table.Row;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

class CsvRow implements Row
{
	private final String[] tokens;
	private final long rowIndex;

	CsvRow(String[] tokens, long rowIndex)
	{
		Preconditions.checkArgument(rowIndex >= 0);
		this.tokens = requireNonNull(tokens);
		this.rowIndex = requireNonNull(rowIndex);
	}

	@Override
	public Stream<Cell> getValues()
	{
		return IntStream.range(0, tokens.length).mapToObj(i -> toCell(tokens[i], i));
	}

	@Override
	public long getIndex()
	{
		return rowIndex;
	}

	private Cell toCell(String token, int columnIndex)
	{
		return token != null ? new CsvCell(token, rowIndex, columnIndex) : null;
	}
}
