package org.molgenis.data.importer.table.excel;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.molgenis.data.importer.table.Table;

import java.util.Iterator;

import static java.util.Objects.requireNonNull;

class ExcelTableIterator implements Iterator<Table>
{
	private final Workbook workbook;
	private int workbookIndex = 0;

	ExcelTableIterator(Workbook workbook)
	{
		this.workbook = requireNonNull(workbook);
	}

	@Override
	public boolean hasNext()
	{
		return workbookIndex < workbook.getNumberOfSheets();
	}

	@Override
	public Table next()
	{
		Sheet sheet = workbook.getSheetAt(workbookIndex);
		++workbookIndex;
		return new ExcelTable(sheet);
	}
}
