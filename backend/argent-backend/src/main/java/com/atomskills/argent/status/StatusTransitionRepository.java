package com.atomskills.argent.status;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * CRUD поверх {@code krn_status_transition} + {@code JpaSpecificationExecutor} — нужен {@link
 * StatusTransitionService} для поиска перехода по тройке (statusType, from, to) с учётом того, что
 * {@code fromStatusId} может быть {@code null} (обычный derived-query-метод с {@code
 * null}-параметром так не умеет).
 */
public interface StatusTransitionRepository
    extends JpaRepository<StatusTransition, UUID>, JpaSpecificationExecutor<StatusTransition> {}
