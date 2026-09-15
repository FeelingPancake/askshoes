package com.atomskills.argent.reference;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * Тело запроса {@code POST}/{@code PUT /api/refs/{code}} — создание/обновление позиции справочника.
 *
 * @param code код позиции (уникален в пределах типа справочника)
 * @param name отображаемое имя
 * @param active активна ли позиция (например, для мягкого скрытия из выпадающих списков)
 * @param sortOrder порядок сортировки при отображении
 * @param attributes произвольные доп. поля, специфичные для типа справочника
 */
public record RefItemRequest(
    @NotBlank @Size(max = 255) String code,
    @NotBlank @Size(max = 255) String name,
    @NotNull Boolean active,
    @NotNull Integer sortOrder,
    Map<String, Object> attributes) {}
