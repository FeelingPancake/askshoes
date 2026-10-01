package com.atomskills.argent.reference.dto;

import com.atomskills.argent.reference.entity.RefItem;
import java.util.Map;
import java.util.UUID;

/**
 * Ответ на операции с позицией справочника — DTO, не сама {@link RefItem}
 *
 * @param id id позиции
 * @param code код позиции
 * @param name отображаемое имя
 * @param active активна ли позиция
 * @param sortOrder порядок сортировки
 * @param attributes произвольные доп. поля
 */
public record RefItemResponse(
    UUID id,
    String code,
    String name,
    Boolean active,
    Integer sortOrder,
    Map<String, Object> attributes) {}
