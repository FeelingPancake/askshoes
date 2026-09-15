package com.atomskills.argent.transfer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Тело запроса на экспорт — те же {@code columns}/{@code rows}, что принимают {@link
 * TableExportService}/{@link DocumentExportService} напрямую, просто как DTO для JSON-запроса.
 */
public record ExportRequest(
    @NotEmpty List<String> columns, @NotNull List<Map<String, Object>> rows) {}
