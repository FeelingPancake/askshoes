package com.atomskills.argent.transfer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;

/** Экспорт табличных данных в документы (PDF/DOCX) */
@Service
public class DocumentExportService {

  private static final float PAGE_MARGIN = 50f;
  private static final float LINE_HEIGHT = 16f;
  private static final float FONT_SIZE = 11f;

  /**
   * @param columns порядок и набор столбцов
   * @param rows строки; значение для столбца {@code c} берётся как {@code row.get(c)}
   * @return содержимое {@code .pdf}-файла
   * @throws IOException если запись документа не удалась
   */
  public byte[] toPdf(List<String> columns, List<Map<String, Object>> rows) throws IOException {
    try (PDDocument document = new PDDocument()) {
      PDFont font = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
      List<String> lines = toTextLines(columns, rows);

      float pageHeight = PDRectangle.A4.getHeight();
      int linesPerPage = (int) ((pageHeight - 2 * PAGE_MARGIN) / LINE_HEIGHT);

      for (int start = 0; start < lines.size(); start += linesPerPage) {
        List<String> pageLines = lines.subList(start, Math.min(start + linesPerPage, lines.size()));
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream content = new PDPageContentStream(document, page)) {
          content.beginText();
          content.setFont(font, FONT_SIZE);
          content.newLineAtOffset(PAGE_MARGIN, pageHeight - PAGE_MARGIN);
          for (String line : pageLines) {
            content.showText(line);
            content.newLineAtOffset(0, -LINE_HEIGHT);
          }
          content.endText();
        }
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      document.save(out);
      return out.toByteArray();
    }
  }

  /**
   * @param columns порядок и набор столбцов (первая строка таблицы — заголовок)
   * @param rows строки; значение для столбца {@code c} берётся как {@code row.get(c)}
   * @return содержимое {@code .docx}-файла
   * @throws IOException если запись документа не удалась
   */
  public byte[] toDocx(List<String> columns, List<Map<String, Object>> rows) throws IOException {
    try (XWPFDocument document = new XWPFDocument()) {
      XWPFTable table = document.createTable();

      XWPFTableRow header = table.getRow(0);
      for (int col = 0; col < columns.size(); col++) {
        cellAt(header, col).setText(columns.get(col));
      }

      for (Map<String, Object> row : rows) {
        XWPFTableRow docxRow = table.createRow();
        for (int col = 0; col < columns.size(); col++) {
          Object value = row.get(columns.get(col));
          cellAt(docxRow, col).setText(value == null ? "" : value.toString());
        }
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      document.write(out);
      return out.toByteArray();
    }
  }

  /** {@code XWPFTableRow} не создаёт клетки сверх тех, что были в исходном 1x1 шаблоне строки. */
  private org.apache.poi.xwpf.usermodel.XWPFTableCell cellAt(XWPFTableRow row, int col) {
    while (row.getTableCells().size() <= col) {
      row.createCell();
    }
    return row.getCell(col);
  }

  private List<String> toTextLines(List<String> columns, List<Map<String, Object>> rows) {
    List<String> lines = new java.util.ArrayList<>();
    lines.add(String.join(" | ", columns));
    for (Map<String, Object> row : rows) {
      lines.add(
          columns.stream()
              .map(column -> String.valueOf(row.get(column)))
              .reduce((a, b) -> a + " | " + b)
              .orElse(""));
    }
    return lines;
  }
}
