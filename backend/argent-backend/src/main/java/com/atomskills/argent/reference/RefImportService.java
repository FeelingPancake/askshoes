package com.atomskills.argent.reference;

import com.atomskills.argent.error.ValidationErrorsException;
import com.atomskills.argent.reference.dto.RefImportResult;
import com.atomskills.argent.reference.dto.RefItemRequest;
import com.atomskills.argent.reference.entity.RefItem;
import com.atomskills.argent.reference.entity.RefType;
import com.atomskills.argent.transfer.TableImportService;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Массовый импорт позиций справочников из одного файла ({@code .xlsx} — лист на справочник, или
 * {@code .json} — ключ на справочник). Upsert по {@code (тип, code)}: существующие позиции
 * обновляются, новые создаются, отсутствующие в файле не трогаются.
 *
 * <p>Всё или ничего: сначала проверяется весь файл и собираются все ошибки сразу; если есть хоть
 * одна — {@link ValidationErrorsException}, в БД ничего не пишется.
 */
@Service
@RequiredArgsConstructor
public class RefImportService {
  private static final Set<String> SYSTEM_COLUMNS = Set.of("code", "name", "sort_order", "active");
  private static final int MAX_LENGTH = 255;

  private final RefTypeRepository refTypeRepository;
  private final RefItemRepository refItemRepository;
  private final TableImportService tableImportService;
  private final JsonMapper jsonMapper;

  /** Позиция из файла вместе с номером её строки — нужен для ключа ошибки. */
  private record ImportRow(int line, RefItemRequest item) {}

  /**
   * @param filename имя загруженного файла — по расширению ({@code .xlsx}/{@code .json}) выбирается
   *     формат
   * @param in содержимое файла; закрывается вызывающим кодом
   * @return счётчики созданных/обновлённых позиций по каждому справочнику из файла
   * @throws IllegalArgumentException если формат не поддерживается или файл не читается
   * @throws ValidationErrorsException если в данных есть ошибки (ничего не записано)
   */
  @Transactional
  public Map<String, RefImportResult> importFile(String filename, InputStream in) {
    String lower = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
    Map<String, String> errors = new LinkedHashMap<>();

    Map<String, List<ImportRow>> rows;
    if (lower.endsWith(".xlsx")) {
      rows = parseXlsx(in, errors);
    } else if (lower.endsWith(".json")) {
      rows = parseJson(in, errors);
    } else {
      throw new IllegalArgumentException("Поддерживаются только файлы .xlsx и .json");
    }

    Map<String, RefType> types = validate(rows, errors);
    if (!errors.isEmpty()) {
      throw new ValidationErrorsException(errors);
    }
    return upsert(rows, types);
  }

  private Map<String, List<ImportRow>> parseXlsx(InputStream in, Map<String, String> errors) {
    Map<String, List<Map<String, String>>> sheets;
    try {
      sheets = tableImportService.fromXlsxAllSheets(in);
    } catch (IOException e) {
      throw new IllegalArgumentException("Не удалось прочитать XLSX-файл");
    }

    Map<String, List<ImportRow>> result = new LinkedHashMap<>();
    sheets.forEach(
        (refCode, sheetRows) -> {
          List<ImportRow> rows = new ArrayList<>();
          for (int i = 0; i < sheetRows.size(); i++) {
            Map<String, String> row = sheetRows.get(i);
            if (row.values().stream().allMatch(String::isBlank)) {
              continue;
            }
            int line = i + 2; // заголовок — строка 1
            String prefix = refCode + ", строка " + line + ", ";
            rows.add(new ImportRow(line, toRequest(row, prefix, errors)));
          }
          result.put(refCode, rows);
        });
    return result;
  }

  /** Строка листа → {@link RefItemRequest}; ошибки приведения {@code sort_order}/{@code active}. */
  private RefItemRequest toRequest(
      Map<String, String> row, String prefix, Map<String, String> errors) {
    String sortOrderRaw = row.getOrDefault("sort_order", "").trim();
    int sortOrder = 0;
    if (!sortOrderRaw.isEmpty()) {
      try {
        sortOrder = Integer.parseInt(sortOrderRaw);
      } catch (NumberFormatException e) {
        errors.put(prefix + "sort_order", "должно быть целым числом");
      }
    }

    String activeRaw = row.getOrDefault("active", "").trim();
    Boolean active = true;
    if (!activeRaw.isEmpty()) {
      active = parseBoolean(activeRaw);
      if (active == null) {
        errors.put(prefix + "active", "должно быть true или false");
      }
    }

    Map<String, Object> attributes = new LinkedHashMap<>();
    row.forEach(
        (column, value) -> {
          if (!SYSTEM_COLUMNS.contains(column) && !value.isBlank()) {
            attributes.put(column, toAttributeValue(value.trim()));
          }
        });

    return new RefItemRequest(
        row.getOrDefault("code", "").trim(),
        row.getOrDefault("name", "").trim(),
        active,
        sortOrder,
        attributes.isEmpty() ? null : attributes);
  }

  /**
   * {@code true}/{@code false} → boolean, число → см. {@link #toJsonNumber}, иначе строка как есть.
   */
  private Object toAttributeValue(String value) {
    Boolean bool = parseBoolean(value);
    if (bool != null) {
      return bool;
    }
    try {
      return toJsonNumber(new BigDecimal(value));
    } catch (NumberFormatException e) {
      return value;
    }
  }

  /**
   * Число в тех же Java-типах, что даёт Jackson при чтении JSON: целое → {@code Integer}/{@code
   * Long}/{@code BigInteger}, дробное → {@code Double}.
   *
   * <p>Не {@link BigDecimal}: Hibernate хранит снимок JSON-поля как копию через сериализацию и
   * сравнивает её с текущим значением через {@code equals}. {@code BigDecimal} после такой копии
   * становится {@code Integer}/{@code Double}, не равен себе прежнему — и Hibernate считает {@code
   * attributes} изменённым всегда: лишний {@code UPDATE} (и запись в аудите) сразу после {@code
   * INSERT} и при каждом повторном импорте.
   */
  private Object toJsonNumber(BigDecimal number) {
    if (number.scale() > 0) {
      return number.doubleValue();
    }
    try {
      return number.intValueExact();
    } catch (ArithmeticException e) {
      // не влезает в int — пробуем шире
    }
    try {
      return number.longValueExact();
    } catch (ArithmeticException e) {
      return number.toBigIntegerExact();
    }
  }

  /** {@code null}, если строка не {@code true}/{@code false} (без учёта регистра). */
  private Boolean parseBoolean(String value) {
    if (value.equalsIgnoreCase("true")) {
      return true;
    }
    if (value.equalsIgnoreCase("false")) {
      return false;
    }
    return null;
  }

  private Map<String, List<ImportRow>> parseJson(InputStream in, Map<String, String> errors) {
    Map<String, List<RefItemRequest>> json;
    try {
      json = jsonMapper.readValue(in, new TypeReference<Map<String, List<RefItemRequest>>>() {});
    } catch (JacksonException e) {
      throw new IllegalArgumentException("Некорректный JSON: " + e.getOriginalMessage());
    }
    if (json == null) {
      throw new IllegalArgumentException("Некорректный JSON: пустой документ");
    }

    Map<String, List<ImportRow>> result = new LinkedHashMap<>();
    json.forEach(
        (refCode, items) -> {
          List<ImportRow> rows = new ArrayList<>();
          List<RefItemRequest> safeItems = items == null ? List.of() : items;
          for (int i = 0; i < safeItems.size(); i++) {
            int line = i + 1;
            RefItemRequest item = safeItems.get(i);
            if (item == null) {
              errors.put(refCode + ", строка " + line, "пустой элемент");
              continue;
            }
            rows.add(
                new ImportRow(
                    line,
                    new RefItemRequest(
                        trimOrNull(item.code()),
                        trimOrNull(item.name()),
                        item.active(),
                        item.sortOrder(),
                        item.attributes())));
          }
          result.put(refCode, rows);
        });
    return result;
  }

  private String trimOrNull(String value) {
    return value == null ? null : value.trim();
  }

  /**
   * Проверки, общие для обоих форматов.
   *
   * @return найденные типы справочников по коду — для upsert
   */
  private Map<String, RefType> validate(
      Map<String, List<ImportRow>> rows, Map<String, String> errors) {
    Map<String, RefType> types = new HashMap<>();
    rows.forEach(
        (refCode, refRows) -> {
          RefType type = refTypeRepository.findByCode(refCode).orElse(null);
          if (type == null) {
            errors.put(refCode, "неизвестный тип справочника");
            return;
          }
          types.put(refCode, type);

          Set<String> seen = new HashSet<>();
          for (ImportRow row : refRows) {
            String prefix = refCode + ", строка " + row.line() + ", ";
            String code = row.item().code();
            checkText(code, prefix + "code", errors);
            checkText(row.item().name(), prefix + "name", errors);
            if (code != null && !code.isEmpty() && !seen.add(code)) {
              errors.put(prefix + "code", "код повторяется в файле");
            }
          }
        });
    return types;
  }

  private void checkText(String value, String key, Map<String, String> errors) {
    if (value == null || value.isEmpty()) {
      errors.put(key, "не заполнено");
    } else if (value.length() > MAX_LENGTH) {
      errors.put(key, "длиннее " + MAX_LENGTH + " символов");
    }
  }

  private Map<String, RefImportResult> upsert(
      Map<String, List<ImportRow>> rows, Map<String, RefType> types) {
    Map<String, RefImportResult> result = new LinkedHashMap<>();
    rows.forEach(
        (refCode, refRows) -> {
          RefType type = types.get(refCode);
          // одним запросом все позиции справочника, а не поиск по каждой строке
          Map<String, RefItem> existing = new HashMap<>();
          for (RefItem item : refItemRepository.findByRefTypeId(type.getId())) {
            existing.put(item.getCode(), item);
          }

          int created = 0;
          int updated = 0;
          for (ImportRow row : refRows) {
            RefItemRequest request = row.item();
            RefItem item = existing.get(request.code());
            if (item == null) {
              item = new RefItem();
              item.setRefTypeId(type.getId());
              item.setCode(request.code());
              created++;
            } else {
              updated++;
            }
            item.setName(request.name());
            item.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
            item.setActive(request.active() == null || request.active());
            item.setAttributes(request.attributes());
            refItemRepository.save(item);
          }
          result.put(refCode, new RefImportResult(created, updated));
        });
    return result;
  }
}
