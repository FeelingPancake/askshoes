package com.atomskills.argent.sequence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data репозиторий для {@link Sequence}. */
public interface SequenceRepository extends JpaRepository<Sequence, UUID> {

  /**
   * Ищет конфигурацию последовательности по коду.
   *
   * @param code код последовательности (уникален)
   * @return последовательность, если существует
   */
  Optional<Sequence> findByCode(String code);
}
