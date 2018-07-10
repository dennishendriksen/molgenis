package org.molgenis.data;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface Row
{
	int getNrOfCells();

	Optional<Cell> getCell(String columnHeader);

	Optional<Cell> getCell(int columnIndex);

	Stream<Cell> getCellStream();

	default void forEachCell(Consumer<Cell> action)
	{
		getCellStream().forEach(action);
	}
}
