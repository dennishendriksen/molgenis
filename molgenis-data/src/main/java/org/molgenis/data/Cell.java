package org.molgenis.data;

import javax.annotation.Nullable;

public interface Cell
{
	enum Type
	{
		STRING
	}

	Type getType();

	@Nullable
	Object getValue();

	@Nullable
	String getValueAsString();

	String getColumnHeader();

	/**
	 * @return zero-based column index
	 */
	int getColumnIndex();

	/**
	 * @return zero-based row index
	 */
	int getRowIndex();
}
