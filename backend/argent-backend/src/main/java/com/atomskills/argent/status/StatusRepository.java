package com.atomskills.argent.status;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Стандартный CRUD поверх {@code krn_status} плюс поиск статуса по коду внутри машины. */
public interface StatusRepository
    extends JpaRepository<Status, UUID>, JpaSpecificationExecutor<Status> {

  /**
   * Ищет статус по коду внутри одной статусной машины ({@code UNIQUE(status_type_id, code)}).
   *
   * @param statusTypeId id статусной машины ({@link StatusType#id})
   * @param code код статуса (например {@code "NEW"})
   * @return статус, если существует
   */
  Optional<Status> findByStatusTypeIdAndCode(UUID statusTypeId, String code);
}
