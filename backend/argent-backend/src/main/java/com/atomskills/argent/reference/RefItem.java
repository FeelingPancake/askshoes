package com.atomskills.argent.reference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Позиция справочника ({@code krn_ref_item}), принадлежит одному {@link RefType} (по {@code
 * refTypeId}). {@code code} уникален в пределах своего типа (не глобально — {@code
 * UNIQUE(ref_type_id, code)} на уровне БД), {@code attributes} — произвольные доп. поля,
 * специфичные для конкретного типа справочника, хранятся как {@code jsonb} через нативную поддержку
 * JSON в Hibernate 6+ (без сторонних библиотек вроде {@code hibernate-types}).
 */
@Table(name = "krn_ref_item")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class RefItem {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  UUID refTypeId;
  String code;
  String name;
  Boolean active;
  Integer sortOrder;

  @JdbcTypeCode(SqlTypes.JSON)
  Map<String, Object> attributes;
}
