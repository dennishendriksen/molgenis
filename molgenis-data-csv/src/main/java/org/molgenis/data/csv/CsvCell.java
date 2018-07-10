package org.molgenis.data.csv;

import org.molgenis.data.Cell;

import javax.annotation.Nullable;
import java.util.Objects;

class CsvCell implements Cell
{
	private final String value;

	CsvCell(@Nullable String value)
	{
		this.value = value;
	}

	@Override
	public Type getType()
	{
		return Type.STRING;
	}

	@Override
	public String getValue()
	{
		return value;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CsvCell csvCell = (CsvCell) o;
		return Objects.equals(value, csvCell.value);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(value);
	}

	@Override
	public String toString()
	{
		return "CsvCell{" + "value='" + value + '\'' + '}';
	}
}
