package com.atomskills.argent.reference;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Универсальный REST для плоских справочников — один контроллер на любое количество {@link RefType}
 * вместо контроллера на каждый справочник.
 */
@RestController
@RequestMapping("api/refs")
@RequiredArgsConstructor
public class ReferenceController {
  private final RefTypeRepository refTypeRepository;
  private final RefItemRepository refItemRepository;

  /**
   * Получение пагинированного списка всех доступных справочников
   *
   * @return страница справочников
   */
  @GetMapping()
  public List<RefTypeResponse> listTypes() {
    return refTypeRepository.findAll(Sort.by("name")).stream().map(RefTypeResponse::from).toList();
  }

  /**
   * Список позиций справочника с пагинацией/сортировкой.
   *
   * @param code код типа справочника (например {@code "ITEM_CATEGORY"})
   * @param pageable страница/размер/сортировка
   * @return страница позиций
   * @throws jakarta.persistence.EntityNotFoundException если такого типа справочника нет
   */
  @GetMapping("{code}")
  public Page<RefItemResponse> list(@PathVariable String code, Pageable pageable) {
    RefType refType =
        refTypeRepository
            .findByCode(code)
            .orElseThrow(() -> new EntityNotFoundException("Unknown reference type: " + code));

    Specification<RefItem> spec =
        (root, query, cb) -> cb.equal(root.get("refTypeId"), refType.getId());

    return refItemRepository.findAll(spec, pageable).map(this::toResponse);
  }

  /**
   * Создаёт новую позицию в указанном справочнике.
   *
   * @param code код типа справочника
   * @param request данные новой позиции
   * @return созданная позиция (с присвоенным {@code id})
   * @throws jakarta.persistence.EntityNotFoundException если такого типа справочника нет
   */
  @PostMapping("{code}")
  public RefItemResponse create(
      @PathVariable String code, @Valid @RequestBody RefItemRequest request) {
    RefType refType =
        refTypeRepository
            .findByCode(code)
            .orElseThrow(() -> new EntityNotFoundException("Unknown reference type: " + code));
    RefItem item = new RefItem();
    item.setRefTypeId(refType.getId());
    item.setCode(request.code());
    item.setName(request.name());
    item.setActive(request.active());
    item.setSortOrder(request.sortOrder());
    item.setAttributes(request.attributes());
    return toResponse(refItemRepository.save(item));
  }

  /**
   * Обновляет существующую позицию справочника (полная замена всех полей из {@code request}).
   *
   * @param code код типа справочника
   * @param itemId id обновляемой позиции
   * @param request новые данные позиции
   * @return обновлённая позиция
   * @throws jakarta.persistence.EntityNotFoundException если позиции с таким {@code itemId} нет
   */
  @PutMapping("{code}/{itemId}")
  public RefItemResponse update(
      @PathVariable String code,
      @PathVariable UUID itemId,
      @Valid @RequestBody RefItemRequest request) {
    RefItem item =
        refItemRepository
            .findById(itemId)
            .orElseThrow(() -> new EntityNotFoundException("Unknown ref item: " + itemId));
    item.setCode(request.code());
    item.setName(request.name());
    item.setActive(request.active());
    item.setSortOrder(request.sortOrder());
    item.setAttributes(request.attributes());
    return toResponse(refItemRepository.save(item));
  }

  /**
   * Удаляет позицию справочника.
   *
   * @param code код типа справочника
   * @param itemId id удаляемой позиции
   */
  @DeleteMapping("{code}/{itemId}")
  public void delete(@PathVariable String code, @PathVariable UUID itemId) {
    refItemRepository.deleteById(itemId);
  }

  private RefItemResponse toResponse(RefItem item) {
    return new RefItemResponse(
        item.getId(),
        item.getCode(),
        item.getName(),
        item.getActive(),
        item.getSortOrder(),
        item.getAttributes());
  }
}
