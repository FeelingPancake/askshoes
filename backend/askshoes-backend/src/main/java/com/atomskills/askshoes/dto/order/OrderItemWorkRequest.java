package com.atomskills.askshoes.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Работа в запросе заказа.
 *
 * @param id {@code null} — новая работа, иначе существующая работа этого изделия
 * @param serviceTypeId позиция {@code SERVICE_TYPE}
 * @param price цена (фронт подставляет цену из справочника, менеджер может править)
 */
public record OrderItemWorkRequest(
    UUID id, @NotNull UUID serviceTypeId, @NotNull @PositiveOrZero BigDecimal price) {}
