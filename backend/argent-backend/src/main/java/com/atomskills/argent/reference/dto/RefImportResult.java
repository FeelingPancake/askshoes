package com.atomskills.argent.reference.dto;

/**
 * Итог импорта одного справочника ({@code POST /api/refs/import}).
 *
 * @param created сколько позиций создано
 * @param updated сколько существующих позиций обновлено
 */
public record RefImportResult(int created, int updated) {}
