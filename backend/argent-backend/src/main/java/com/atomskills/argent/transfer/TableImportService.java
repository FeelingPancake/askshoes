package com.atomskills.argent.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

/**
 * Разбор загруженного XLSX/CSV в строки (первая строка — заголовок, задаёт ключи map'ов). Не знает,
 * что дальше делать с этими строками — это решает вызывающий домен (например, массовая загрузка
 * позиций справочника через {@code argent.reference}).
 *
 * <p>Оба формата возвращают одинаковый по форме результат — {@code List<Map<String, String>>}, всё
 * строками (даже числовые ячейки XLSX — через {@link DataFormatter}, тот же приём, что показывает
 * Excel пользователю). Приведение к нужным типам (число, дата и т.д.) — забота вызывающего кода,
 * который знает, что именно он импортирует.
 */
@Service
public class TableImportService {

  /**
   * @param in содержимое {@code .xlsx}-файла; закрывается вызывающим кодом, не этим методом
   * @return строки первого листа; первая строка файла — заголовок, не попадает в результат как
   *     данные
   * @throws IOException если файл повреждён или не читается
   */
  public List<Map<String, String>> fromXlsx(InputStream in) throws IOException {
    List<Map<String, String>> result = new ArrayList<>();
    DataFormatter formatter = new DataFormatter();

    try (XSSFWorkbook workbook = new XSSFWorkbook(in)) {
      Sheet sheet = workbook.getSheetAt(0);
      Row header = sheet.getRow(0);
      if (header == null) {
        return result;
      }

      List<String> columns = new ArrayList<>();
      for (int col = 0; col < header.getLastCellNum(); col++) {
        columns.add(formatter.formatCellValue(header.getCell(col)));
      }

      for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
          continue;
        }
        Map<String, String> rowMap = new java.util.LinkedHashMap<>();
        for (int col = 0; col < columns.size(); col++) {
          rowMap.put(columns.get(col), formatter.formatCellValue(row.getCell(col)));
        }
        result.add(rowMap);
      }
    }

    return result;
  }

  /**
   * @param in содержимое {@code .csv}-файла (ожидается UTF-8); закрывается вызывающим кодом
   * @return строки; заголовок (первая строка файла) определяет ключи, в результат как данные не
   *     попадает
   * @throws IOException если файл повреждён или не читается
   */
  public List<Map<String, String>> fromCsv(InputStream in) throws IOException {
    CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();

    try (CSVParser parser =
        CSVParser.parse(new InputStreamReader(in, StandardCharsets.UTF_8), format)) {
      List<Map<String, String>> result = new ArrayList<>();
      parser.forEach(record -> result.add(record.toMap()));
      return result;
    }
  }
}
