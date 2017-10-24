package org.molgenis.data.importer.table.csv;

import au.com.bytecode.opencsv.CSVReader;
import org.molgenis.data.importer.table.Row;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;

class CsvRowIterator implements Iterator<Row>
{
	private final CSVReader csvReader;
	private boolean getNext = true;
	private Row next;
	private long index = 0L;

	CsvRowIterator(CSVReader csvReader)
	{
		this.csvReader = requireNonNull(csvReader);
	}

	@Override
	public boolean hasNext()
	{
		return get() != null;
	}

	@Override
	public Row next()
	{
		Row row = get();
		if (row == null)
		{
			throw new NoSuchElementException();
		}
		getNext = true;
		return row;
	}

	private Row get()
	{
		if (getNext)
		{
			String[] tokens;
			try
			{
				tokens = csvReader.readNext();
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}

			next = tokens != null ? new CsvRow(tokens, index) : null;
			index++;
			getNext = false;
		}
		return next;
	}
}
