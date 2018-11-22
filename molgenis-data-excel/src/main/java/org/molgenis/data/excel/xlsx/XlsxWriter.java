package org.molgenis.data.excel.xlsx;

import static java.util.Objects.requireNonNull;
import static jdk.nashorn.internal.runtime.JSType.toLong;
import static org.molgenis.data.DataConverter.toBoolean;
import static org.molgenis.data.DataConverter.toDouble;
import static org.molgenis.data.DataConverter.toInstant;
import static org.molgenis.data.DataConverter.toInt;
import static org.molgenis.data.DataConverter.toLocalDate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.molgenis.data.excel.xlsx.exception.MaximumSheetNameLengthExceededException;
import org.molgenis.data.excel.xlsx.exception.UnsupportedValueException;

public class XlsxWriter implements AutoCloseable {

  // Apache poi library cuts of sheet names at 31 characters
  public static final int MAXIMUM_SHEET_LENGTH = 31;
  private final Path target;
  private final Workbook workbook;

  XlsxWriter(Path target, Workbook workbook) {
    this.target = requireNonNull(target);
    this.workbook = requireNonNull(workbook);
  }

  public boolean hasSheet(String name) {
    return workbook.getSheet(name) != null;
  }

  public void createSheet(String name, List<Object> headers) {
    if (name.length() <= MAXIMUM_SHEET_LENGTH) {
      Sheet sheet = workbook.getSheet(name);
      if (sheet == null) {
        sheet = workbook.createSheet(name);
        internalWriteRow(headers, sheet, 0);
      }
    } else {
      throw new MaximumSheetNameLengthExceededException(name);
    }
  }

  private void internalWriteRow(List<Object> values, Sheet sheet, int rowNr) {
    final org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNr);
    AtomicInteger counter = new AtomicInteger(0);
    values
        .stream()
        .forEach(
            record -> {
              int index = counter.getAndIncrement();
              if (record != null) {
                createCell(row, index, record);
              }
            });
  }

  public void writeRow(List<Object> row, String sheetName) {
    this.writeRows(Stream.of(row), sheetName);
  }

  public void writeRows(List<List<Object>> rows, String sheetName) {
    this.writeRows(rows.stream(), sheetName);
  }

  public void writeRows(Stream<List<Object>> rows, String sheetName) {
    Sheet sheet = workbook.getSheet(sheetName);
    rows.forEach(
        row -> {
          internalWriteRow(row, sheet, sheet.getLastRowNum() + 1);
        });
  }

  public void close() throws IOException {
    try {
      workbook.write(Files.newOutputStream(target));
    } finally {
      workbook.close();
    }
  }

  public void createCell(Row row, int index, Object value) {
    Cell cell = row.createCell(++index);
    if (value instanceof Boolean) {
      cell.setCellValue(toBoolean(value));
    } else if (value instanceof Date) {
      //TODO: system default okay?
      cell.setCellValue(Date.from(toLocalDate(value).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    } else if (value instanceof Instant) {
      cell.setCellValue(Date.from(toInstant(value)));
    } else if (value instanceof Double) {
      cell.setCellValue(toDouble(value));
    } else if (value instanceof Integer) {
      cell.setCellValue(toInt(value));
    } else if (value instanceof Long) {
      cell.setCellValue(toLong(value));
    } else {
      throw new UnsupportedValueException(value);
    }
  }
}