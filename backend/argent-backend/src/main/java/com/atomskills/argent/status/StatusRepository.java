package com.atomskills.argent.status;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Стандартный CRUD поверх {@code krn_status} — своих методов пока не нужно. */
public interface StatusRepository
    extends JpaRepository<Status, UUID>, JpaSpecificationExecutor<Status> {}
