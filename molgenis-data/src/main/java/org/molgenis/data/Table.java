package org.molgenis.data;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface Table extends AutoCloseable
{
	String getId();

	int getNrOfRows();

	Stream<Row> getRowStream();

	default void forEachRow(Consumer<Row> action)
	{
		getRowStream().forEach(action);
	}
}
