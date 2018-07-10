package org.molgenis.data;

import java.util.Optional;
import java.util.stream.Stream;

public interface Tables extends AutoCloseable
{
	boolean hasTable(String tableId);

	Optional<Table> getTable(String tableId);

	Stream<Table> getTableStream();
}
