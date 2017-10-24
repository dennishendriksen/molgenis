package org.molgenis.data.importer.table.excel;

import org.molgenis.data.importer.table.Cell;
import org.molgenis.data.importer.table.CellType;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.apache.poi.ss.usermodel.Cell.*;
import static org.molgenis.data.importer.table.CellType.*;

class ExcelCell implements Cell
{
	private final org.apache.poi.ss.usermodel.Cell cell;

	ExcelCell(org.apache.poi.ss.usermodel.Cell cell)
	{
		this.cell = requireNonNull(cell);
	}

	@Override
	public CellType getType()
	{
		CellType cellType;
		switch (cell.getCellType())
		{
			case CELL_TYPE_BLANK:
				cellType = CellType.STRING; // TODO hmmm
				break;
			case CELL_TYPE_NUMERIC:
				cellType = CellType.NUMERIC;
				break;
			case CELL_TYPE_STRING:
				cellType = CellType.STRING;
				break;
			case CELL_TYPE_FORMULA:
				throw new RuntimeException("TODO support formula evaluation");
			case CELL_TYPE_BOOLEAN:
				cellType = CellType.BOOLEAN;
				break;
			case CELL_TYPE_ERROR:
				throw new RuntimeException("TODO throw nice exception here (CELL_TYPE_ERROR)");
			default:
				throw new RuntimeException("TODO throw nice exception here (unknown cell type");
		}
		return cellType;
	}

	@Override
	public String getStringValue()
	{
		String value;
		switch (cell.getCellType())
		{
			case CELL_TYPE_BLANK:
				value = null;
				break;
			case CELL_TYPE_NUMERIC:
				throw new IllegalArgumentException(format("Cell type is '%s' instead of '%s'", STRING, NUMERIC));
			case CELL_TYPE_STRING:
				value = cell.getStringCellValue();
				break;
			case CELL_TYPE_FORMULA:
				throw new RuntimeException("TODO support formula evaluation");
			case CELL_TYPE_BOOLEAN:
				throw new IllegalArgumentException(format("Cell type is '%s' instead of '%s'", STRING, BOOLEAN));
			case CELL_TYPE_ERROR:
				throw new RuntimeException("TODO throw nice exception here (CELL_TYPE_ERROR)");
			default:
				throw new RuntimeException("TODO throw nice exception here (unknown cell type");
		}
		return value;
	}

	@Override
	public Double getNumericValue()
	{
		Double value;
		switch (cell.getCellType())
		{
			case CELL_TYPE_BLANK:
				value = null;
				break;
			case CELL_TYPE_NUMERIC:
				value = cell.getNumericCellValue();
				break;
			case CELL_TYPE_STRING:
				throw new IllegalArgumentException(format("Cell type is '%s' instead of '%s'", STRING, NUMERIC));
			case CELL_TYPE_FORMULA:
				throw new RuntimeException("TODO support formula evaluation");
			case CELL_TYPE_BOOLEAN:
				throw new IllegalArgumentException(format("Cell type is '%s' instead of '%s'", STRING, BOOLEAN));
			case CELL_TYPE_ERROR:
				throw new RuntimeException("TODO throw nice exception here (CELL_TYPE_ERROR)");
			default:
				throw new RuntimeException("TODO throw nice exception here (unknown cell type");
		}
		return value;
	}

	@Override
	public Boolean getBooleanValue()
	{
		Boolean value;
		switch (cell.getCellType())
		{
			case CELL_TYPE_BLANK:
				value = null;
				break;
			case CELL_TYPE_NUMERIC:
				throw new IllegalArgumentException(format("Cell type is '%s' instead of '%s'", NUMERIC, BOOLEAN));
			case CELL_TYPE_STRING:
				throw new IllegalArgumentException(format("Cell type is '%s' instead of '%s'", STRING, BOOLEAN));
			case CELL_TYPE_FORMULA:
				throw new RuntimeException("TODO support formula evaluation");
			case CELL_TYPE_BOOLEAN:
				value = cell.getBooleanCellValue();
				break;
			case CELL_TYPE_ERROR:
				throw new RuntimeException("TODO throw nice exception here (CELL_TYPE_ERROR)");
			default:
				throw new RuntimeException("TODO throw nice exception here (unknown cell type");
		}
		return value;
	}

	@Override
	public long getRowIndex()
	{
		return cell.getRowIndex();
	}

	@Override
	public long getColumnIndex()
	{
		return cell.getColumnIndex();
	}
}
