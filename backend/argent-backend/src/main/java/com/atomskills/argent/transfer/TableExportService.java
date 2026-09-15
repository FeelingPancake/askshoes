package com.atomskills.argent.transfer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

/**
 * Экспорт произвольных табличных данных (не привязан ни к какой сущности домена — вызывающий код
 * сам решает, что именно экспортировать) в XLSX/CSV.
 *
 * <p>Контракт входных данных единый для всех форматов: {@code columns} задаёт порядок и набор
 * столбцов (в отличие от порядка ключей {@code Map}, который ничем не гарантирован), {@code rows} —
 * сами данные, где значение столбца ищется по ключу из {@code columns} в каждой строке.
 */
@Service
public class TableExportService {

  /**
   * @param columns порядок и набор столбцов
   * @param rows строки; значение для столбца {@code c} берётся как {@code row.get(c)}
   * @return содержимое {@code .xlsx}-файла
   * @throws IOException если запись книги в память не удалась
   */
  public byte[] toXlsx(List<String> columns, List<Map<String, Object>> rows) throws IOException {
    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet();

      Row header = sheet.createRow(0);
      for (int col = 0; col < columns.size(); col++) {
        header.createCell(col).setCellValue(columns.get(col));
      }

      int rowIndex = 1;
      for (Map<String, Object> row : rows) {
        Row xlsxRow = sheet.createRow(rowIndex++);
        for (int col = 0; col < columns.size(); col++) {
          setCellValue(xlsxRow.createCell(col), row.get(columns.get(col)));
        }
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      workbook.write(out);
      return out.toByteArray();
    }
  }

  /**
   * @param columns порядок и набор столбцов (первая строка CSV — заголовок)
   * @param rows строки; значение для столбца {@code c} берётся как {@code row.get(c)}
   * @return содержимое {@code .csv}-файла (UTF-8)
   * @throws IOException если запись не удалась
   */
  public byte[] toCsv(List<String> columns, List<Map<String, Object>> rows) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CSVFormat format =
        CSVFormat.DEFAULT.builder().setHeader(columns.toArray(new String[0])).build();

    try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        CSVPrinter printer = new CSVPrinter(writer, format)) {
      for (Map<String, Object> row : rows) {
        printer.printRecord(columns.stream().map(row::get).toList());
      }
    }

    return out.toByteArray();
  }

  private void setCellValue(Cell cell, Object value) {
    switch (value) {
      case null -> {}
      case Number number -> cell.setCellValue(number.doubleValue());
      case Boolean bool -> cell.setCellValue(bool);
      default -> cell.setCellValue(value.toString());
    }
  }
}
