package com.atomskills.askshoes.service;

import com.atomskills.argent.error.ValidationErrorsException;
import com.atomskills.argent.reference.RefItemRepository;
import com.atomskills.askshoes.entity.client.Client;
import com.atomskills.askshoes.entity.order.Order;
import com.atomskills.askshoes.entity.order.OrderItem;
import com.atomskills.askshoes.entity.order.OrderItemWork;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Суммы заказа: работа → изделие → заказ → скидка → итог. Пишет результат прямо в сущности.
 *
 * <p>Бин без изменяемого состояния: промежуточные суммы — локальные переменные, поэтому, в отличие
 * от {@link OrderValidation}, отдельный объект на вызов не нужен.
 *
 * <p>Заодно проверяет {@code paidAmount <= total}: это правило не может жить в {@link
 * OrderValidator} — до расчёта итог ещё неизвестен.
 */
@Service
@RequiredArgsConstructor
public class OrderCalculator {
  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  private final RefItemRepository refItemRepository;

  /**
   * Пересчитывает {@code total} работ, {@code worksTotal} изделий и все суммы заказа; работы
   * гарантийного изделия стоят 0. Скидка округляется до копеек ({@code HALF_UP}).
   *
   * @throws ValidationErrorsException если оплачено больше итога ({@code paidAmount})
   */
  public void recalc(Order order) {
    BigDecimal worksTotal = BigDecimal.ZERO;
    for (OrderItem item : order.getItems()) {
      worksTotal = worksTotal.add(recalcItem(item));
    }

    BigDecimal discountPercent = discountPercent(order.getClient());
    BigDecimal discountAmount =
        worksTotal.multiply(discountPercent).divide(HUNDRED, 2, RoundingMode.HALF_UP);

    order.setWorksTotal(worksTotal);
    order.setDiscountPercent(discountPercent);
    order.setDiscountAmount(discountAmount);
    order.setTotal(worksTotal.subtract(discountAmount));

    if (order.getPaidAmount().compareTo(order.getTotal()) > 0) {
      throw new ValidationErrorsException(Map.of("paidAmount", "Оплачено больше суммы заказа"));
    }
  }

  /**
   * @return сумма работ изделия — она же записана в {@code worksTotal}
   */
  private BigDecimal recalcItem(OrderItem orderItem) {
    BigDecimal itemTotal = BigDecimal.ZERO;
    for (OrderItemWork work : orderItem.getWorks()) {
      work.setTotal(
          Boolean.TRUE.equals(orderItem.getWarranty()) ? BigDecimal.ZERO : work.getPrice());
      itemTotal = itemTotal.add(work.getTotal());
    }
    orderItem.setWorksTotal(itemTotal);

    return itemTotal;
  }

  /** Скидка из {@code CLIENT_STATUS.attributes.discountPercent}; нет статуса или ключа — 0. */
  private BigDecimal discountPercent(Client client) {
    if (client.getStatusId() == null) {
      return BigDecimal.ZERO;
    }
    return refItemRepository
        .findById(client.getStatusId())
        .map(status -> RefLookup.attribute(status, "discountPercent"))
        .map(value -> new BigDecimal(value.toString()))
        .orElse(BigDecimal.ZERO);
  }
}
