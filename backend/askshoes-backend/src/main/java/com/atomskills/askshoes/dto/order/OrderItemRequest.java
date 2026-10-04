package com.atomskills.askshoes.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Изделие в запросе заказа.
 *
 * @param id {@code null} — новое изделие, иначе существующее изделие этого заказа
 * @param categoryId позиция {@code ITEM_CATEGORY}
 * @param itemTypeId позиция {@code ITEM_TYPE}, её {@code categoryCode} должен совпасть с категорией
 * @param brandId позиция {@code BRAND}
 * @param warranty гарантийный случай — только для бренда с {@code warranty = true}
 * @param colorId позиция {@code COLOR}
 * @param materialId позиция {@code MATERIAL}
 * @param sizeId позиция {@code ITEM_SIZE} из сетки типа изделия
 * @param wearPercent износ, 0–100
 * @param comment комментарий
 * @param defectTypeIds позиции {@code DEFECT_TYPE}; отсутствие — пустой список, повторы
 *     схлопываются
 * @param works работы; отсутствие — пустой список
 */
public record OrderItemRequest(
    UUID id,
    @NotNull UUID categoryId,
    @NotNull UUID itemTypeId,
    UUID brandId,
    @NotNull Boolean warranty,
    UUID colorId,
    UUID materialId,
    UUID sizeId,
    @Min(0) @Max(100) Integer wearPercent,
    String comment,
    List<@NotNull UUID> defectTypeIds,
    List<@NotNull @Valid OrderItemWorkRequest> works) {

  /** Отсутствующие {@code defectTypeIds} и {@code works} трактуются как пустые. */
  public OrderItemRequest {
    defectTypeIds = defectTypeIds == null ? List.of() : defectTypeIds;
    works = works == null ? List.of() : works;
  }
}
