package com.atomskills.argent.reference;

import java.util.List;
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
}
