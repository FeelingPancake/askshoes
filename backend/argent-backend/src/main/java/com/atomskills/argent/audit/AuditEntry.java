package com.atomskills.argent.audit;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Одна запись журнала изменений — одно изменённое поле одной сущности в один момент времени.
 *
 * <p>Заполняется не вручную, а слушателем событий Hibernate (см. {@link com.atomskills.argent.audit
 * package-info})
 */
@Entity
@Table(name = "krn_audit_entry")
@Getter
@Setter
public class AuditEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  /** Простое имя класса изменённой сущности, например {@code "Order"}. */
  String entityType;

  /** Строковое представление id изменённой сущности (не обязательно UUID). */
  String entityId;

  /** {@code INSERT}/{@code UPDATE}/{@code DELETE}. */
  String action;

  /** Изменённое поле; {@code null} для {@code INSERT}/{@code DELETE} (меняется вся сущность). */
  String field;

  String oldValue;
  String newValue;

  /** Кто внёс изменение; {@code null}, если изменение сделано не от имени пользователя. */
  UUID actorId;

  String actorName;
  Instant createdAt;
}
