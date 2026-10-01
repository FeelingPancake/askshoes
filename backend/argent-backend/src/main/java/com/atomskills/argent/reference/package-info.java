/**
 * Универсальный слой справочников: {@code krn_ref_type} (список типов справочников) + {@code
 * krn_ref_item} (позиции внутри каждого типа), один REST-контроллер ({@code POST/GET/PUT/DELETE
 * /api/refs/{code}}) на любое количество плоских справочников.
 *
 * <p>{@link com.atomskills.argent.reference.RefImportService} — массовый импорт позиций многих
 * справочников одним файлом ({@code POST /api/refs/import}, XLSX: лист = справочник, JSON: ключ =
 * справочник). Upsert по {@code code}, всё или ничего: ошибки собираются по всему файлу и
 * возвращаются разом ({@link com.atomskills.argent.error.ValidationErrorsException}). Типы
 * справочников импорт не создаёт — они живут в миграциях.
 */
package com.atomskills.argent.reference;
