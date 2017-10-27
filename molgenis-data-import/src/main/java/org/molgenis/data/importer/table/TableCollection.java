package org.molgenis.data.importer.table;

import java.util.stream.Stream;

public interface TableCollection
{
	/**
	 * @throws java.io.UncheckedIOException when an I/O exception occurred
	 */
	Stream<Table> getTableStream();
}
