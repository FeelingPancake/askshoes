package com.atomskills.askshoes.service;

import com.atomskills.argent.reference.RefItemRepository;
import com.atomskills.argent.reference.RefTypeRepository;
import com.atomskills.argent.reference.entity.RefItem;
import com.atomskills.argent.reference.entity.RefType;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Позиции справочников одного запроса, загруженные заранее: все {@code id} — одним {@code
 * findAllById}, а не запросом на каждое поле.
 *
 * <p>Объект на один вызов, не бин: набор позиций свой у каждого запроса. Только ищет — ошибки и
 * пути полей решает вызывающий код.
 */
final class RefLookup {
  private final Map<UUID, RefItem> itemsById;
  private final Map<String, UUID> typeIdsByCode;

  /**
   * @param ids все {@code id} позиций, которые понадобятся {@link #find}; без {@code null}
   */
  RefLookup(
      RefItemRepository refItemRepository, RefTypeRepository refTypeRepository, Set<UUID> ids) {
    this.itemsById =
        refItemRepository.findAllById(ids).stream()
            .collect(Collectors.toMap(RefItem::getId, Function.identity()));
    this.typeIdsByCode =
        refTypeRepository.findAll().stream()
            .collect(Collectors.toMap(RefType::getCode, RefType::getId));
  }

  /**
   * @param typeCode код справочника, например {@code BRAND}
   * @return позиция, если она есть и принадлежит справочнику {@code typeCode}; иначе {@code null}
   * @throws IllegalStateException если справочника {@code typeCode} нет — это ошибка кода или
   *     миграций, а не запроса
   */
  RefItem find(UUID id, String typeCode) {
    UUID refTypeId = typeIdsByCode.get(typeCode);
    if (refTypeId == null) {
      throw new IllegalStateException("Нет справочника " + typeCode);
    }
    RefItem item = itemsById.get(id);
    return item != null && refTypeId.equals(item.getRefTypeId()) ? item : null;
  }

  /**
   * @return значение {@code attributes[key]} позиции; {@code null}, если атрибутов или ключа нет
   */
  static Object attribute(RefItem item, String key) {
    return item.getAttributes() == null ? null : item.getAttributes().get(key);
  }
}
