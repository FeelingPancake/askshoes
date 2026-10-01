/**
 * Экспорт/импорт табличных данных
 *
 * <p>{@link com.atomskills.argent.transfer.TableExportService} — экспорт в XLSX/CSV; {@link
 * com.atomskills.argent.transfer.DocumentExportService} — экспорт в PDF/DOCX (только экспорт).
 * {@link com.atomskills.argent.transfer.TableImportService} — импорт из XLSX/CSV (первый лист) и
 * всех листов XLSX сразу ({@code fromXlsxAllSheets} — для импорта, где лист = сущность). Все три
 * сервиса работают с единым контрактом: список столбцов ({@code columns}, задаёт порядок и состав)
 * + список строк ({@code rows}, {@code Map<String, Object>}/{@code Map<String, String>}, ключи — из
 * {@code columns}).
 *
 * <p>{@link com.atomskills.argent.transfer.TransferController} — один REST-контроллер на оба
 * направления ({@code POST /api/export/{format}}, {@code POST /api/import/{format}})
 */
package com.atomskills.argent.transfer;
