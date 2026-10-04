package com.atomskills.askshoes.dto.order;

import com.atomskills.askshoes.dto.client.ClientResponse;
import com.atomskills.askshoes.entity.order.Order;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Заказ целиком — ответ {@code POST}/{@code PUT}/{@code GET /api/orders/{id}}. Имена справочников
 * не кладутся: фронт берёт их из загруженных справочников.
 *
 * @param id id заказа
 * @param number номер заказа
 * @param acceptedAt когда принят
 * @param status статус заказа
 * @param client клиент
 * @param dueDate срок готовности
 * @param note примечание
 * @param itemsCount количество изделий
 * @param worksTotal сумма работ
 * @param discountPercent скидка, % (из статуса клиента)
 * @param discountAmount скидка, руб.
 * @param total итого к оплате
 * @param paidAmount оплачено
 * @param amountDue осталось оплатить ({@code total − paidAmount})
 * @param items изделия по {@code seqNo}
 */
public record OrderResponse(
    UUID id,
    String number,
    Instant acceptedAt,
    StatusInfo status,
    ClientResponse client,
    LocalDate dueDate,
    String note,
    int itemsCount,
    BigDecimal worksTotal,
    BigDecimal discountPercent,
    BigDecimal discountAmount,
    BigDecimal total,
    BigDecimal paidAmount,
    BigDecimal amountDue,
    List<OrderItemResponse> items) {

  /**
   * Статус заказа в ответе.
   *
   * @param code код ({@code NEW})
   * @param name отображаемое имя
   */
  public record StatusInfo(String code, String name) {}

  /**
   * @param order заказ (ленивые связи должны быть доступны — вызывать в транзакции)
   * @return DTO заказа с номерами изделий и {@code amountDue}
   */
  public static OrderResponse from(Order order) {
    int itemsCount = order.getItems().size();
    return new OrderResponse(
        order.getId(),
        order.getNumber(),
        order.getAcceptedAt(),
        new StatusInfo(order.getStatus().getCode(), order.getStatus().getName()),
        ClientResponse.from(order.getClient()),
        order.getDueDate(),
        order.getNote(),
        itemsCount,
        order.getWorksTotal(),
        order.getDiscountPercent(),
        order.getDiscountAmount(),
        order.getTotal(),
        order.getPaidAmount(),
        order.getTotal().subtract(order.getPaidAmount()),
        order.getItems().stream()
            .map(item -> OrderItemResponse.from(item, order.getNumber(), itemsCount))
            .toList());
  }
}
