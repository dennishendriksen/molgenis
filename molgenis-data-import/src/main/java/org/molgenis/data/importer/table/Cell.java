package org.molgenis.data.importer.table;

import javax.annotation.Nullable;

public interface Cell
{
	CellType getType();

	/**
	 * @return String or null
	 * @throws IllegalArgumentException if cell type is not {@link CellType#STRING}
	 */
	@Nullable
	String getStringValue();

	/**
	 * @return Double or null
	 * @throws IllegalArgumentException if cell type is not {@link CellType#NUMERIC}
	 */
	@Nullable
	Double getNumericValue();

	/**
	 * @return Boolean or null
	 * @throws IllegalArgumentException if cell type is not {@link CellType#BOOLEAN}
	 */
	@Nullable
	Boolean getBooleanValue();

	/**
	 * @return zero-based row index
	 */
	long getRowIndex();

	/**
	 * @return zero-based column index
	 */
	long getColumnIndex();
}
