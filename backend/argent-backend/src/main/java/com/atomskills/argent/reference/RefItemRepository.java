package com.atomskills.argent.reference;

import com.atomskills.argent.reference.entity.RefItem;
import com.atomskills.argent.reference.entity.RefType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Spring Data репозиторий для {@link RefItem}. */
public interface RefItemRepository
    extends JpaRepository<RefItem, UUID>, JpaSpecificationExecutor<RefItem> {

  /**
   * Все позиции конкретного типа справочника.
   *
   * @param refTypeId id типа ({@link RefType#id})
   * @return позиции этого типа (в произвольном порядке — сортировку задаёт вызывающий через {@link
   *     org.springframework.data.domain.Pageable})
   */
  List<RefItem> findByRefTypeId(UUID refTypeId);

  /**
   * Позиция справочника по коду ({@code code} уникален в пределах типа).
   *
   * @param refTypeId id типа ({@link RefType#id})
   * @param code код позиции
   * @return позиция, если существует
   */
  Optional<RefItem> findByRefTypeIdAndCode(UUID refTypeId, String code);
}
