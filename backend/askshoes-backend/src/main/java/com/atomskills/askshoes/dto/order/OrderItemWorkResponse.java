package com.atomskills.askshoes.dto.order;

import com.atomskills.askshoes.entity.order.OrderItemWork;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Работа в ответе заказа.
 *
 * @param id id работы
 * @param serviceTypeId позиция {@code SERVICE_TYPE}
 * @param price цена
 * @param total к оплате по работе ({@code 0} у гарантийного изделия)
 */
public record OrderItemWorkResponse(
    UUID id, UUID serviceTypeId, BigDecimal price, BigDecimal total) {

  /**
   * @param work работа
   * @return DTO работы
   */
  public static OrderItemWorkResponse from(OrderItemWork work) {
    return new OrderItemWorkResponse(
        work.getId(), work.getServiceTypeId(), work.getPrice(), work.getTotal());
  }
}
