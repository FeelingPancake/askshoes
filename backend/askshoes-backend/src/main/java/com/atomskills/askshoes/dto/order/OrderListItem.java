package com.atomskills.askshoes.dto.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Строка списка заказов — плоская, с готовыми строками для таблицы.
 *
 * @param id id заказа
 * @param number номер заказа
 * @param acceptedAt когда принят
 * @param clientName ФИО клиента
 * @param clientPhone телефон клиента
 * @param statusName имя статуса
 * @param itemsCount количество изделий
 * @param total итого к оплате
 * @param amountDue осталось оплатить
 * @param dueDate срок готовности
 */
public record OrderListItem(
    UUID id,
    String number,
    Instant acceptedAt,
    String clientName,
    String clientPhone,
    String statusName,
    int itemsCount,
    BigDecimal total,
    BigDecimal amountDue,
    LocalDate dueDate) {}
