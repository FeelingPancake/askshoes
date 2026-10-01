package com.atomskills.argent.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
 *
 * <p>Числа в XLSX форматируются в {@link Locale#ROOT} (точка как десятичный разделитель) — иначе на
 * машине с русской локалью {@code 1500.5} пришло бы как {@code "1500,5"} и не распарсилось бы как
 * число. Колонки с пустым заголовком пропускаются, заголовки обрезаются по краям.
 */
@Service
public class TableImportService {

  /**
   * @param in содержимое {@code .xlsx}-файла; закрывается вызывающим кодом, не этим методом
   * @return строки первого листа; первая строка файла — заголовок, не попадает в результат как
   *     данные; физически отсутствующие строки пропускаются
   * @throws IOException если файл повреждён или не читается
   */
  public List<Map<String, String>> fromXlsx(InputStream in) throws IOException {
    try (XSSFWorkbook workbook = new XSSFWorkbook(in)) {
      return readSheet(workbook.getSheetAt(0), new DataFormatter(Locale.ROOT)).stream()
          .filter(row -> !row.isEmpty())
          .toList();
    }
  }

  /**
   * Читает все листы книги — для импорта, где каждый лист описывает отдельную сущность (например,
   * лист = справочник).
   *
   * <p>В отличие от {@link #fromXlsx}, строки <b>не пропускаются</b>: элемент списка с индексом
   * {@code i} — это строка Excel номер {@code i + 2} (заголовок — строка 1). Так вызывающий код
   * может сообщить пользователю точный номер строки в ошибке. Физически отсутствующая строка
   * приходит пустой map; решать, что делать с пустыми строками, — забота вызывающего кода.
   *
   * @param in содержимое {@code .xlsx}-файла; закрывается вызывающим кодом, не этим методом
   * @return ключ — имя листа, значение — его строки; порядок листов как в книге
   * @throws IOException если файл повреждён или не читается
   */
  public Map<String, List<Map<String, String>>> fromXlsxAllSheets(InputStream in)
      throws IOException {
    Map<String, List<Map<String, String>>> result = new LinkedHashMap<>();
    DataFormatter formatter = new DataFormatter(Locale.ROOT);

    try (XSSFWorkbook workbook = new XSSFWorkbook(in)) {
      for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
        Sheet sheet = workbook.getSheetAt(i);
        result.put(sheet.getSheetName(), readSheet(sheet, formatter));
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

  /** Все строки листа после заголовка, по одному элементу на строку; отсутствующая — пустая map. */
  private List<Map<String, String>> readSheet(Sheet sheet, DataFormatter formatter) {
    List<Map<String, String>> result = new ArrayList<>();
    Row header = sheet.getRow(0);
    if (header == null) {
      return result;
    }

    // индекс колонки → имя; колонки без заголовка не попадают, поэтому индексы могут идти с дырами
    Map<Integer, String> columns = new LinkedHashMap<>();
    for (int col = 0; col < header.getLastCellNum(); col++) {
      String name = formatter.formatCellValue(header.getCell(col)).trim();
      if (!name.isEmpty()) {
        columns.put(col, name);
      }
    }

    for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
      Row row = sheet.getRow(rowIndex);
      Map<String, String> rowMap = new LinkedHashMap<>();
      if (row != null) {
        columns.forEach(
            (col, name) -> rowMap.put(name, formatter.formatCellValue(row.getCell(col))));
      }
      result.add(rowMap);
    }

    return result;
  }
}
