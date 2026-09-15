package com.atomskills.argent.audit;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Стандартный CRUD поверх {@code krn_audit_entry} — своих методов пока не нужно. */
public interface AuditEntryRepository extends JpaRepository<AuditEntry, UUID> {}
