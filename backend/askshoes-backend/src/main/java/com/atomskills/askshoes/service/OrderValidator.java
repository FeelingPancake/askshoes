package com.atomskills.askshoes.service;

import com.atomskills.argent.error.ValidationErrorsException;
import com.atomskills.argent.reference.RefItemRepository;
import com.atomskills.argent.reference.RefTypeRepository;
import com.atomskills.askshoes.dto.client.ClientRequest;
import com.atomskills.askshoes.dto.order.OrderItemRequest;
import com.atomskills.askshoes.dto.order.OrderRequest;
import com.atomskills.askshoes.entity.order.Order;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Бизнес-проверка формы приёмки заказа — точка входа для {@link OrderService}.
 *
 * <p>Бин — синглтон, его вызывают параллельные запросы, поэтому изменяемого состояния здесь нет:
 * каждый вызов загружает свой {@link RefLookup} и создаёт свой {@link OrderValidation}, где и
 * копятся ошибки.
 */
@Service
@RequiredArgsConstructor
public class OrderValidator {
  private final RefItemRepository refItemRepository;
  private final RefTypeRepository refTypeRepository;

  /**
   * Проверяет запрос целиком и собирает все ошибки за один проход.
   *
   * @param existing заказ для PUT, {@code null} для POST
   * @throws ValidationErrorsException если есть хотя бы одна ошибка; ключи — пути полей формы в
   *     порядке полей
   */
  public void validate(OrderRequest request, Order existing) {
    RefLookup refs = new RefLookup(refItemRepository, refTypeRepository, collectRefIds(request));
    new OrderValidation(request, existing, refs).run();
  }

  /** Все {@code id} позиций справочников из запроса — чтобы загрузить их одним запросом. */
  private static Set<UUID> collectRefIds(OrderRequest request) {
    Set<UUID> ids = new HashSet<>();
    ClientRequest client = request.client();
    ids.add(client.sourceId());
    ids.add(client.statusId());
    ids.addAll(client.contactMethodIds());
    for (OrderItemRequest item : request.items()) {
      ids.addAll(
          Arrays.asList(
              item.categoryId(),
              item.itemTypeId(),
              item.brandId(),
              item.colorId(),
              item.materialId(),
              item.sizeId()));
      ids.addAll(item.defectTypeIds());
      item.works().forEach(work -> ids.add(work.serviceTypeId()));
    }
    ids.remove(null);
    return ids;
  }
}
