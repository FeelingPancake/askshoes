package com.atomskills.argent.transfer;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST для {@code argent.transfer}
 *
 * <p>{@code {format}} в обоих направлениях — один из {@code xlsx}/{@code csv} (экспорт и импорт)
 * или {@code pdf}/{@code docx} (только экспорт, см. {@link DocumentExportService}).
 */
@RestController
@RequiredArgsConstructor
public class TransferController {
  private final TableExportService tableExportService;
  private final DocumentExportService documentExportService;
  private final TableImportService tableImportService;

  /**
   * Экспортирует переданные табличные данные в файл нужного формата.
   *
   * @param format {@code xlsx}/{@code csv}/{@code pdf}/{@code docx}
   * @param request данные для экспорта ({@code columns} — порядок столбцов, {@code rows} — строки)
   * @return файл с корректными {@code Content-Type}/{@code Content-Disposition}
   * @throws IOException если генерация файла не удалась
   */
  @PostMapping("api/export/{format}")
  public ResponseEntity<ByteArrayResource> export(
      @PathVariable String format, @Valid @RequestBody ExportRequest request) throws IOException {
    byte[] content;
    MediaType contentType;

    switch (format.toLowerCase()) {
      case "xlsx" -> {
        content = tableExportService.toXlsx(request.columns(), request.rows());
        contentType =
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      }
      case "csv" -> {
        content = tableExportService.toCsv(request.columns(), request.rows());
        contentType = MediaType.parseMediaType("text/csv; charset=UTF-8");
      }
      case "pdf" -> {
        content = documentExportService.toPdf(request.columns(), request.rows());
        contentType = MediaType.APPLICATION_PDF;
      }
      case "docx" -> {
        content = documentExportService.toDocx(request.columns(), request.rows());
        contentType =
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
      }
      default ->
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "Неизвестный формат экспорта: " + format);
    }

    ContentDisposition contentDisposition =
        ContentDisposition.attachment()
            .filename("export." + format.toLowerCase(), StandardCharsets.UTF_8)
            .build();

    return ResponseEntity.ok()
        .contentType(contentType)
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        .header("X-Content-Type-Options", "nosniff")
        .body(new ByteArrayResource(content));
  }

  /**
   * Разбирает загруженный файл в строки.
   *
   * @param format {@code xlsx}/{@code csv}
   * @param file содержимое файла (multipart/form-data)
   * @return строки файла (первая строка файла — заголовок, в результат как данные не попадает)
   * @throws IOException если файл повреждён или не читается
   */
  @PostMapping("api/import/{format}")
  public List<Map<String, String>> importFile(
      @PathVariable String format, @RequestParam MultipartFile file) throws IOException {
    return switch (format.toLowerCase()) {
      case "xlsx" -> tableImportService.fromXlsx(file.getInputStream());
      case "csv" -> tableImportService.fromCsv(file.getInputStream());
      default ->
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "Неизвестный формат импорта: " + format);
    };
  }
}
