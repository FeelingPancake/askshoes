package com.atomskills.argent.status;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Стандартный CRUD поверх {@code krn_status_type} плюс поиск по коду. */
public interface StatusTypeRepository
    extends JpaRepository<StatusType, UUID>, JpaSpecificationExecutor<StatusType> {

  /**
   * Ищет статусную машину по коду (например {@code "ORDER"}).
   *
   * @param code код статусной машины
   * @return статусная машина, если существует
   */
  Optional<StatusType> findByCode(String code);
}
