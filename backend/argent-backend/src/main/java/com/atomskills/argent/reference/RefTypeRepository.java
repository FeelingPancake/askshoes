package com.atomskills.argent.reference;

import com.atomskills.argent.reference.entity.RefType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data репозиторий для {@link RefType}. */
public interface RefTypeRepository extends JpaRepository<RefType, UUID> {

  /**
   * Ищет тип справочника по коду (например {@code "ITEM_CATEGORY"}).
   *
   * @param code код типа (уникален)
   * @return тип справочника, если существует
   */
  Optional<RefType> findByCode(String code);
}
