package com.atomskills.argent.reference;

import com.atomskills.argent.reference.dto.RefImportResult;
import com.atomskills.argent.reference.dto.RefItemRequest;
import com.atomskills.argent.reference.dto.RefItemResponse;
import com.atomskills.argent.reference.dto.RefTypeResponse;
import com.atomskills.argent.reference.entity.RefItem;
import com.atomskills.argent.reference.entity.RefType;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
  private final RefImportService refImportService;

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
   * Импорт позиций сразу нескольких справочников из одного файла (upsert по {@code code}, всё или
   * ничего). Формат файла — см. {@link RefImportService}.
   *
   * <p>Литеральный путь {@code import} приоритетнее шаблонного {@code {code}}, поэтому запрос не
   * попадает в {@link #create}.
   *
   * @param file {@code .xlsx} (лист = справочник) или {@code .json} (ключ = справочник)
   * @return созданные/обновлённые позиции по каждому справочнику
   * @throws IOException если не удалось прочитать загруженный файл
   * @throws com.atomskills.argent.error.ValidationErrorsException если в данных есть ошибки
   */
  @PostMapping(value = "import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Map<String, RefImportResult> importFile(@RequestParam MultipartFile file)
      throws IOException {
    try (InputStream in = file.getInputStream()) {
      return refImportService.importFile(file.getOriginalFilename(), in);
    }
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
