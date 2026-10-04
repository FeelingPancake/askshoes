package com.atomskills.askshoes.service;

import static com.atomskills.askshoes.service.RefLookup.attribute;

import com.atomskills.argent.error.ValidationErrorsException;
import com.atomskills.argent.reference.entity.RefItem;
import com.atomskills.askshoes.dto.client.ClientRequest;
import com.atomskills.askshoes.dto.order.OrderItemRequest;
import com.atomskills.askshoes.dto.order.OrderItemWorkRequest;
import com.atomskills.askshoes.dto.order.OrderRequest;
import com.atomskills.askshoes.entity.order.Order;
import com.atomskills.askshoes.entity.order.OrderItem;
import com.atomskills.askshoes.utils.PhoneUtils;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Одна проверка одного запроса (method object): то, что было бы локальными переменными длинного
 * метода, — поля, поэтому методам проверки не нужно протаскивать контекст параметрами.
 *
 * <p>Не бин: создаётся {@link OrderValidator} на каждый вызов и живёт в одном потоке, поэтому
 * изменяемые поля здесь безопасны.
 *
 * <p>Порядок проверок = порядок полей формы: клиент → по каждому изделию справочники, правила,
 * {@code id}, работы. {@link LinkedHashMap} сохраняет этот порядок в ответе.
 */
final class OrderValidation {
  private static final String CLIENT_SOURCE = "CLIENT_SOURCE";
  private static final String CLIENT_STATUS = "CLIENT_STATUS";
  private static final String CONTACT_METHOD = "CONTACT_METHOD";
  private static final String ITEM_CATEGORY = "ITEM_CATEGORY";
  private static final String ITEM_TYPE = "ITEM_TYPE";
  private static final String BRAND = "BRAND";
  private static final String COLOR = "COLOR";
  private static final String MATERIAL = "MATERIAL";
  private static final String ITEM_SIZE = "ITEM_SIZE";
  private static final String DEFECT_TYPE = "DEFECT_TYPE";
  private static final String SERVICE_TYPE = "SERVICE_TYPE";

  private final OrderRequest request;

  /** Заказ для PUT, {@code null} для POST. */
  private final Order existing;

  private final RefLookup refs;
  private final Map<UUID, OrderItem> existingItems;
  private final Set<UUID> seenItemIds = new HashSet<>();
  private final Set<UUID> seenWorkIds = new HashSet<>();
  private final Map<String, String> errors = new LinkedHashMap<>();

  OrderValidation(OrderRequest request, Order existing, RefLookup refs) {
    this.request = request;
    this.existing = existing;
    this.refs = refs;
    this.existingItems =
        existing == null
            ? Map.of()
            : existing.getItems().stream()
                .collect(Collectors.toMap(OrderItem::getId, Function.identity()));
  }

  /**
   * @throws ValidationErrorsException если есть хотя бы одна ошибка
   */
  void run() {
    validateClient(request.client());
    for (int i = 0; i < request.items().size(); i++) {
      validateItem(request.items().get(i), "items[" + i + "]");
    }
    if (!errors.isEmpty()) {
      throw new ValidationErrorsException(errors);
    }
  }

  private void validateClient(ClientRequest client) {
    if (PhoneUtils.normalizePhone(client.phone()) == null) {
      error("client.phone", PhoneUtils.INVALID_PHONE_MESSAGE);
    }
    ref("client.sourceId", client.sourceId(), CLIENT_SOURCE);
    ref("client.statusId", client.statusId(), CLIENT_STATUS);
    for (int i = 0; i < client.contactMethodIds().size(); i++) {
      ref("client.contactMethodIds[" + i + "]", client.contactMethodIds().get(i), CONTACT_METHOD);
    }
  }

  private void validateItem(OrderItemRequest item, String path) {
    RefItem category = ref(path + ".categoryId", item.categoryId(), ITEM_CATEGORY);
    RefItem itemType = ref(path + ".itemTypeId", item.itemTypeId(), ITEM_TYPE);
    RefItem brand = ref(path + ".brandId", item.brandId(), BRAND);
    ref(path + ".colorId", item.colorId(), COLOR);
    ref(path + ".materialId", item.materialId(), MATERIAL);
    RefItem size = ref(path + ".sizeId", item.sizeId(), ITEM_SIZE);
    for (int j = 0; j < item.defectTypeIds().size(); j++) {
      ref(path + ".defectTypeIds[" + j + "]", item.defectTypeIds().get(j), DEFECT_TYPE);
    }

    checkTypeBelongsToCategory(path, category, itemType);
    checkSizeFitsGrid(path, item, itemType, size);
    checkWarranty(path, item, brand);

    OrderItem existingItem = checkItemId(path, item.id());
    for (int k = 0; k < item.works().size(); k++) {
      validateWork(item.works().get(k), path + ".works[" + k + "]", existingItem);
    }
  }

  private void validateWork(OrderItemWorkRequest work, String path, OrderItem existingItem) {
    ref(path + ".serviceTypeId", work.serviceTypeId(), SERVICE_TYPE);
    checkWorkId(path, work.id(), existingItem);
  }

  /** {@code ITEM_TYPE.attributes.categoryCode} совпадает с кодом выбранной категории. */
  private void checkTypeBelongsToCategory(String path, RefItem category, RefItem itemType) {
    if (category != null
        && itemType != null
        && !Objects.equals(attribute(itemType, "categoryCode"), category.getCode())) {
      error(path + ".itemTypeId", "Тип изделия не относится к выбранной категории");
    }
  }

  /**
   * Размер указан только у типа с сеткой ({@code ITEM_TYPE.attributes.sizeGrid}) и только из этой
   * сетки ({@code ITEM_SIZE.attributes.gridCode}).
   */
  private void checkSizeFitsGrid(
      String path, OrderItemRequest item, RefItem itemType, RefItem size) {
    if (itemType == null || item.sizeId() == null) {
      return;
    }
    Object grid = attribute(itemType, "sizeGrid");
    if (grid == null) {
      error(path + ".sizeId", "У этого типа изделия нет размера");
    } else if (size != null && !Objects.equals(grid, attribute(size, "gridCode"))) {
      error(path + ".sizeId", "Размер не из сетки этого типа изделия");
    }
  }

  /**
   * Гарантия — только у бренда с {@code BRAND.attributes.warranty = true}. Если бренд не найден,
   * ошибка уже есть у {@code brandId}, вторую не добавляем.
   */
  private void checkWarranty(String path, OrderItemRequest item, RefItem brand) {
    boolean warrantyBrand = brand != null && Boolean.TRUE.equals(attribute(brand, "warranty"));
    boolean brandUnknown = item.brandId() != null && brand == null;
    if (Boolean.TRUE.equals(item.warranty()) && !warrantyBrand && !brandUnknown) {
      error(path + ".warranty", "Гарантия возможна только для гарантийного бренда");
    }
  }

  /**
   * {@code id} изделия: только в PUT, только из этого заказа, не дважды.
   *
   * @return существующее изделие, если {@code id} корректен; иначе {@code null}
   */
  private OrderItem checkItemId(String path, UUID id) {
    if (id == null) {
      return null;
    }
    if (existing == null) {
      error(path + ".id", "У нового заказа не может быть существующих изделий");
      return null;
    }
    OrderItem found = existingItems.get(id);
    if (found == null) {
      error(path + ".id", "Изделие не найдено в этом заказе");
      return null;
    }
    if (!seenItemIds.add(id)) {
      error(path + ".id", "Изделие указано в запросе дважды");
      return null;
    }
    return found;
  }

  /**
   * {@code id} работы: только в PUT, только у этого же изделия, не дважды.
   *
   * @param existingItem результат {@link #checkItemId}; {@code null} — у изделия нет корректного
   *     {@code id}, значит, своих работ у него быть не может
   */
  private void checkWorkId(String path, UUID id, OrderItem existingItem) {
    if (id == null) {
      return;
    }
    if (existing == null) {
      error(path + ".id", "У нового заказа не может быть существующих работ");
      return;
    }
    boolean belongs =
        existingItem != null
            && existingItem.getWorks().stream().anyMatch(work -> id.equals(work.getId()));
    if (!belongs) {
      error(path + ".id", "Работа не найдена у этого изделия");
    } else if (!seenWorkIds.add(id)) {
      error(path + ".id", "Работа указана в запросе дважды");
    }
  }

  /**
   * Пустое поле — не ошибка; указанный, но не найденный {@code id} — ошибка по {@code path}.
   *
   * @return позиция или {@code null}
   */
  private RefItem ref(String path, UUID id, String typeCode) {
    if (id == null) {
      return null;
    }
    RefItem item = refs.find(id, typeCode);
    if (item == null) {
      error(path, "Нет такой позиции в справочнике " + typeCode);
    }
    return item;
  }

  /**
   * Единственный способ записать ошибку. {@code putIfAbsent}: первая ошибка по полю — самая точная
   * (справочники проверяются раньше правил), последующие её не перезаписывают.
   */
  private void error(String path, String message) {
    errors.putIfAbsent(path, message);
  }
}
