package com.atomskills.argent.attachment;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** CRUD поверх {@code krn_attachment} + поиск по привязанной сущности. */
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

  /** Все вложения конкретной сущности (оба параметра обязательны — {@code null}-проблемы нет). */
  List<Attachment> findByEntityTypeAndEntityId(String entityType, String entityId);
}
