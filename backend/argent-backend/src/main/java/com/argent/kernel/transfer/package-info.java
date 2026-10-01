/**
 * Экспорт/импорт табличных данных
 *
 * <p>{@link com.argent.kernel.transfer.TableExportService} — экспорт в XLSX/CSV; {@link
 * com.argent.kernel.transfer.DocumentExportService} — экспорт в PDF/DOCX (только экспорт).
 * {@link com.argent.kernel.transfer.TableImportService} — импорт из XLSX/CSV (первый лист) и
 * всех листов XLSX сразу ({@code fromXlsxAllSheets} — для импорта, где лист = сущность). Все три
 * сервиса работают с единым контрактом: список столбцов ({@code columns}, задаёт порядок и состав)
 * + список строк ({@code rows}, {@code Map<String, Object>}/{@code Map<String, String>}, ключи — из
 * {@code columns}).
 *
 * <p>{@link com.argent.kernel.transfer.TransferController} — один REST-контроллер на оба
 * направления ({@code POST /api/export/{format}}, {@code POST /api/import/{format}})
 */
package com.argent.kernel.transfer;
