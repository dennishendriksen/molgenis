package org.molgenis.data.importer.table;

import java.util.stream.Stream;

public interface Row
{
	/**
	 * @throws java.io.UncheckedIOException when an I/O exception occurred
	 */
	Stream<Cell> getValues();

	long getIndex();
}
