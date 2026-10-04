package com.atomskills.askshoes.dto.order;

import com.atomskills.askshoes.dto.client.ClientRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Тело {@code POST /api/orders} и {@code PUT /api/orders/{id}} — вся форма приёмки целиком. Суммы,
 * кроме {@code paidAmount} и цен работ, клиент не присылает.
 *
 * @param client данные клиента — найдётся по телефону или будет создан
 * @param dueDate срок готовности
 * @param note примечание
 * @param paidAmount уже оплачено; {@code null} — {@code 0}
 * @param items изделия; порядок задаёт {@code seqNo}
 */
public record OrderRequest(
    @NotNull @Valid ClientRequest client,
    LocalDate dueDate,
    String note,
    @PositiveOrZero BigDecimal paidAmount,
    @NotEmpty List<@NotNull @Valid OrderItemRequest> items) {}
