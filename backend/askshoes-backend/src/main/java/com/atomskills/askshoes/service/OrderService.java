package com.atomskills.askshoes.service;

import com.atomskills.argent.error.ValidationErrorsException;
import com.atomskills.argent.reference.RefItemRepository;
import com.atomskills.argent.reference.RefTypeRepository;
import com.atomskills.argent.reference.entity.RefItem;
import com.atomskills.argent.reference.entity.RefType;
import com.atomskills.argent.sequence.NumberGenerator;
import com.atomskills.argent.status.Status;
import com.atomskills.argent.status.StatusRepository;
import com.atomskills.argent.status.StatusTypeRepository;
import com.atomskills.askshoes.dto.client.ClientRequest;
import com.atomskills.askshoes.dto.order.*;
import com.atomskills.askshoes.entity.client.Client;
import com.atomskills.askshoes.entity.order.Order;
import com.atomskills.askshoes.entity.order.OrderItem;
import com.atomskills.askshoes.entity.order.OrderItemDefect;
import com.atomskills.askshoes.entity.order.OrderItemWork;
import com.atomskills.askshoes.repository.OrderRepository;
import com.atomskills.askshoes.utils.PhoneUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Приёмка заказа как одного агрегата. Каждая операция — одна транзакция: проверка запроса целиком
 * (все ошибки за один проход, до первой записи) → upsert клиента → номер → синхронизация изделий,
 * работ и повреждений → пересчёт сумм.
 *
 * <p>{@link NumberGenerator} пишет через {@code JdbcTemplate} в том же соединении, что и JPA,
 * поэтому при любой ошибке счётчик номеров откатывается вместе с заказом.
 */
@Service
@RequiredArgsConstructor
public class OrderService {
  private static final String SEQUENCE_CODE = "ORDER";
  private static final String STATUS_TYPE_CODE = "ORDER";
  private static final String INITIAL_STATUS_CODE = "NEW";
  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  private final OrderRepository orderRepository;
  private final ClientService clientService;
  private final RefItemRepository refItemRepository;
  private final RefTypeRepository refTypeRepository;
  private final NumberGenerator numberGenerator;
  private final StatusTypeRepository statusTypeRepository;
  private final StatusRepository statusRepository;

  /**
   * Создаёт заказ: номер из последовательности {@code ORDER}, статус {@code ORDER/NEW}.
   *
   * @param request форма приёмки
   * @param managerId кто принимает заказ ({@code krn_user.id})
   * @return созданный заказ
   * @throws ValidationErrorsException если запрос не прошёл бизнес-проверки — в БД ничего не
   *     записано, счётчик номеров не сдвинут
   */
  @Transactional
  public OrderResponse create(OrderRequest request, UUID managerId) {
    validate(request, null);
    Client client = clientService.upsertByPhone(request.client());

    Order order = new Order();
    order.setNumber(numberGenerator.generateNumber(SEQUENCE_CODE));
    order.setAcceptedAt(Instant.now());
    order.setManagerId(managerId);
    order.setStatus(initialStatus());
    order.setClient(client);

    apply(order, request);
    recalc(order);
    return OrderResponse.from(orderRepository.saveAndFlush(order));
  }

  /**
   * Сохраняет форму приёмки поверх существующего заказа: изделия и работы синхронизируются по
   * {@code id}, отсутствующие в запросе удаляются, {@code seqNo} перенумеровывается.
   *
   * @param id id заказа
   * @param request форма приёмки
   * @return сохранённый заказ
   * @throws EntityNotFoundException если заказа нет
   * @throws ValidationErrorsException если запрос не прошёл бизнес-проверки
   */
  @Transactional
  public OrderResponse update(UUID id, OrderRequest request) {
    Order order = findOrder(id);
    validate(request, order);
    order.setClient(clientService.upsertByPhone(request.client()));

    apply(order, request);
    recalc(order);
    return OrderResponse.from(orderRepository.saveAndFlush(order));
  }

  /**
   * @param id id заказа
   * @return заказ целиком
   * @throws EntityNotFoundException если заказа нет
   */
  @Transactional(readOnly = true)
  public OrderResponse get(UUID id) {
    return OrderResponse.from(findOrder(id));
  }

  /**
   * Список заказов. {@code search} — подстрока без учёта регистра в номере заказа или ФИО клиента,
   * либо цифры из {@code search} в телефоне клиента (если цифры в нём есть).
   *
   * @param search строка поиска; {@code null}/пустая — без фильтра
   * @param pageable страница/размер/сортировка
   * @return страница строк списка
   */
  @Transactional(readOnly = true)
  public Page<OrderListItem> list(String search, Pageable pageable) {
    return orderRepository.findAll(searchSpec(search), pageable).map(this::toListItem);
  }

  private Specification<Order> searchSpec(String search) {
    return (root, query, cb) -> {
      if (search == null || search.isBlank()) {
        return cb.conjunction();
      }
      String pattern = "%" + search.trim().toLowerCase() + "%";
      Join<Order, Client> client = root.join("client");
      List<Predicate> anyOf = new ArrayList<>();
      anyOf.add(cb.like(cb.lower(root.get("number")), pattern));
      anyOf.add(cb.like(cb.lower(client.get("fullName")), pattern));
      // Без цифр условие по телефону выродилось бы в LIKE '%%' и совпало бы со всеми заказами.
      String digits = search.replaceAll("\\D", "");
      if (!digits.isEmpty()) {
        anyOf.add(cb.like(client.get("phone"), "%" + digits + "%"));
      }
      return cb.or(anyOf.toArray(Predicate[]::new));
    };
  }

  private OrderListItem toListItem(Order order) {
    return new OrderListItem(
        order.getId(),
        order.getNumber(),
        order.getAcceptedAt(),
        order.getClient().getFullName(),
        order.getClient().getPhone(),
        order.getStatus().getName(),
        order.getItemsCount() == null ? 0 : order.getItemsCount().intValue(),
        order.getTotal(),
        order.getTotal().subtract(order.getPaidAmount()),
        order.getDueDate());
  }

  private Order findOrder(UUID id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Заказ не найден: " + id));
  }

  private Status initialStatus() {
    return statusTypeRepository
        .findByCode(STATUS_TYPE_CODE)
        .flatMap(
            type -> statusRepository.findByStatusTypeIdAndCode(type.getId(), INITIAL_STATUS_CODE))
        .orElseThrow(
            () ->
                new NoSuchElementException(
                    "Нет статуса " + STATUS_TYPE_CODE + "/" + INITIAL_STATUS_CODE));
  }

  // ---------------------------------------------------------------- validate

  /**
   * Все бизнес-проверки
   *
   * @param existing заказ для PUT, {@code null} для POST
   */
  private void validate(OrderRequest request, Order existing) {
    Map<String, String> errors = new LinkedHashMap<>();
    RefLookup refs = new RefLookup(request, errors);

    ClientRequest client = request.client();
    if (PhoneUtils.normalizePhone(client.phone()) == null) {
      errors.put("client.phone", PhoneUtils.INVALID_PHONE_MESSAGE);
    }
    refs.get("client.sourceId", client.sourceId(), "CLIENT_SOURCE");
    refs.get("client.statusId", client.statusId(), "CLIENT_STATUS");
    for (int i = 0; i < client.contactMethodIds().size(); i++) {
      refs.get(
          "client.contactMethodIds[" + i + "]", client.contactMethodIds().get(i), "CONTACT_METHOD");
    }

    Map<UUID, OrderItem> existingItems =
        existing == null
            ? Map.of()
            : existing.getItems().stream()
                .collect(Collectors.toMap(OrderItem::getId, Function.identity()));
    Set<UUID> seenItemIds = new HashSet<>();
    Set<UUID> seenWorkIds = new HashSet<>();

    for (int i = 0; i < request.items().size(); i++) {
      OrderItemRequest item = request.items().get(i);
      String path = "items[" + i + "]";

      RefItem category = refs.get(path + ".categoryId", item.categoryId(), "ITEM_CATEGORY");
      RefItem itemType = refs.get(path + ".itemTypeId", item.itemTypeId(), "ITEM_TYPE");
      RefItem brand = refs.get(path + ".brandId", item.brandId(), "BRAND");
      refs.get(path + ".colorId", item.colorId(), "COLOR");
      refs.get(path + ".materialId", item.materialId(), "MATERIAL");
      RefItem size = refs.get(path + ".sizeId", item.sizeId(), "ITEM_SIZE");
      for (int j = 0; j < item.defectTypeIds().size(); j++) {
        refs.get(path + ".defectTypeIds[" + j + "]", item.defectTypeIds().get(j), "DEFECT_TYPE");
      }

      if (category != null
          && itemType != null
          && !Objects.equals(attribute(itemType, "categoryCode"), category.getCode())) {
        errors.put(path + ".itemTypeId", "Тип изделия не относится к выбранной категории");
      }

      if (itemType != null && item.sizeId() != null) {
        Object grid = attribute(itemType, "sizeGrid");
        if (grid == null) {
          errors.put(path + ".sizeId", "У этого типа изделия нет размера");
        } else if (size != null && !Objects.equals(grid, attribute(size, "gridCode"))) {
          errors.put(path + ".sizeId", "Размер не из сетки этого типа изделия");
        }
      }

      boolean warrantyBrand = brand != null && Boolean.TRUE.equals(attribute(brand, "warranty"));
      boolean brandUnknown = item.brandId() != null && brand == null;
      if (Boolean.TRUE.equals(item.warranty()) && !warrantyBrand && !brandUnknown) {
        errors.put(path + ".warranty", "Гарантия возможна только для гарантийного бренда");
      }

      OrderItem existingItem =
          checkItemId(item.id(), path, existing, existingItems, seenItemIds, errors);

      for (int k = 0; k < item.works().size(); k++) {
        OrderItemWorkRequest work = item.works().get(k);
        String workPath = path + ".works[" + k + "]";
        refs.get(workPath + ".serviceTypeId", work.serviceTypeId(), "SERVICE_TYPE");
        checkWorkId(work.id(), workPath, existing, item.id(), existingItem, seenWorkIds, errors);
      }
    }

    if (!errors.isEmpty()) {
      throw new ValidationErrorsException(errors);
    }
  }

  /** Проверяет {@code id} изделия; возвращает существующее изделие, если оно найдено. */
  private OrderItem checkItemId(
      UUID id,
      String path,
      Order existing,
      Map<UUID, OrderItem> existingItems,
      Set<UUID> seenItemIds,
      Map<String, String> errors) {
    if (id == null) {
      return null;
    }
    if (existing == null) {
      errors.put(path + ".id", "У нового заказа не может быть существующих изделий");
      return null;
    }
    OrderItem found = existingItems.get(id);
    if (found == null) {
      errors.put(path + ".id", "Изделие не найдено в этом заказе");
      return null;
    }
    if (!seenItemIds.add(id)) {
      errors.put(path + ".id", "Изделие указано в запросе дважды");
      return null;
    }
    return found;
  }

  private void checkWorkId(
      UUID id,
      String path,
      Order existing,
      UUID itemId,
      OrderItem existingItem,
      Set<UUID> seenWorkIds,
      Map<String, String> errors) {
    if (id == null) {
      return;
    }
    if (existing == null) {
      errors.put(path + ".id", "У нового заказа не может быть существующих работ");
      return;
    }
    boolean belongs =
        itemId != null
            && existingItem != null
            && existingItem.getWorks().stream().anyMatch(work -> id.equals(work.getId()));
    if (!belongs) {
      errors.put(path + ".id", "Работа не найдена у этого изделия");
    } else if (!seenWorkIds.add(id)) {
      errors.put(path + ".id", "Работа указана в запросе дважды");
    }
  }

  private static Object attribute(RefItem item, String key) {
    return item.getAttributes() == null ? null : item.getAttributes().get(key);
  }

  /**
   * Все позиции справочников из запроса, загруженные одним {@code findAllById}, и проверка, что
   * позиция существует и принадлежит ожидаемому типу.
   */
  private final class RefLookup {
    private final Map<UUID, RefItem> items;
    private final Map<String, UUID> typeIdsByCode;
    private final Map<String, String> errors;

    RefLookup(OrderRequest request, Map<String, String> errors) {
      this.errors = errors;
      this.items =
          refItemRepository.findAllById(collectRefIds(request)).stream()
              .collect(Collectors.toMap(RefItem::getId, Function.identity()));
      this.typeIdsByCode =
          refTypeRepository.findAll().stream()
              .collect(Collectors.toMap(RefType::getCode, RefType::getId));
    }

    /**
     * @return позиция, если {@code id} указан, существует и нужного типа; иначе {@code null} (и
     *     ошибка по {@code path}, если {@code id} был указан)
     */
    RefItem get(String path, UUID id, String typeCode) {
      if (id == null) {
        return null;
      }
      RefItem item = items.get(id);
      if (item == null || !Objects.equals(item.getRefTypeId(), typeIdsByCode.get(typeCode))) {
        errors.put(path, "Нет такой позиции в справочнике " + typeCode);
        return null;
      }
      return item;
    }

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

  // ------------------------------------------------------------------- apply

  /** Переносит поля запроса в заказ и синхронизирует изделия, работы и повреждения. */
  private void apply(Order order, OrderRequest request) {
    order.setDueDate(request.dueDate());
    order.setNote(request.note());
    order.setPaidAmount(request.paidAmount() == null ? BigDecimal.ZERO : request.paidAmount());

    Map<UUID, OrderItem> existing = new HashMap<>();
    order.getItems().forEach(item -> existing.put(item.getId(), item));
    Set<OrderItem> kept = new HashSet<>();

    for (int i = 0; i < request.items().size(); i++) {
      OrderItemRequest itemRequest = request.items().get(i);
      OrderItem item;
      if (itemRequest.id() == null) {
        item = new OrderItem();
        item.setOrder(order);
        order.getItems().add(item);
      } else {
        item = existing.get(itemRequest.id());
      }

      item.setSeqNo(i + 1);
      item.setCategoryId(itemRequest.categoryId());
      item.setItemTypeId(itemRequest.itemTypeId());
      item.setBrandId(itemRequest.brandId());
      item.setWarranty(itemRequest.warranty());
      item.setColorId(itemRequest.colorId());
      item.setMaterialId(itemRequest.materialId());
      item.setSizeId(itemRequest.sizeId());
      item.setWearPercent(itemRequest.wearPercent());
      item.setComment(itemRequest.comment());
      syncWorks(item, itemRequest.works());
      syncDefects(item, new LinkedHashSet<>(itemRequest.defectTypeIds()));
      kept.add(item);
    }

    order.getItems().removeIf(item -> !kept.contains(item));
  }

  private void syncWorks(OrderItem item, List<OrderItemWorkRequest> requests) {
    Map<UUID, OrderItemWork> existing = new HashMap<>();
    item.getWorks().forEach(work -> existing.put(work.getId(), work));
    Set<OrderItemWork> kept = new HashSet<>();

    for (OrderItemWorkRequest request : requests) {
      OrderItemWork work;
      if (request.id() == null) {
        work = new OrderItemWork();
        work.setItem(item);
        item.getWorks().add(work);
      } else {
        work = existing.get(request.id());
      }
      work.setServiceTypeId(request.serviceTypeId());
      work.setPrice(request.price());
      kept.add(work);
    }

    item.getWorks().removeIf(work -> !kept.contains(work));
  }

  /**
   * По разнице, а не {@code clear()} + {@code add()}: Hibernate при flush выполняет INSERT раньше
   * DELETE, и повторно добавленный тот же {@code defect_type_id} нарушил бы unique.
   */
  private void syncDefects(OrderItem item, Set<UUID> wanted) {
    item.getDefects().removeIf(defect -> !wanted.contains(defect.getDefectTypeId()));

    Set<UUID> present =
        item.getDefects().stream()
            .map(OrderItemDefect::getDefectTypeId)
            .collect(Collectors.toSet());
    for (UUID defectTypeId : wanted) {
      if (!present.contains(defectTypeId)) {
        OrderItemDefect defect = new OrderItemDefect();
        defect.setItem(item);
        defect.setDefectTypeId(defectTypeId);
        item.getDefects().add(defect);
      }
    }
  }

  // ------------------------------------------------------------------ recalc

  /**
   * Пересчитывает все суммы заказа; затем проверяет, что оплачено не больше итога.
   *
   * @throws ValidationErrorsException если {@code paidAmount > total}
   */
  private void recalc(Order order) {
    BigDecimal worksTotal = BigDecimal.ZERO;
    for (OrderItem item : order.getItems()) {
      BigDecimal itemTotal = BigDecimal.ZERO;
      for (OrderItemWork work : item.getWorks()) {
        work.setTotal(Boolean.TRUE.equals(item.getWarranty()) ? BigDecimal.ZERO : work.getPrice());
        itemTotal = itemTotal.add(work.getTotal());
      }
      item.setWorksTotal(itemTotal);
      worksTotal = worksTotal.add(itemTotal);
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

  /** Скидка из {@code CLIENT_STATUS.attributes.discountPercent}; нет статуса или ключа — 0. */
  private BigDecimal discountPercent(Client client) {
    if (client.getStatusId() == null) {
      return BigDecimal.ZERO;
    }
    return refItemRepository
        .findById(client.getStatusId())
        .map(status -> attribute(status, "discountPercent"))
        .map(value -> new BigDecimal(value.toString()))
        .orElse(BigDecimal.ZERO);
  }
}
