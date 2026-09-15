package com.atomskills.argent.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Тип справочника ({@code krn_ref_type}) — например {@code ITEM_CATEGORY}. Сам по себе не несёт
 * данных для UI, только группирует {@link RefItem} по {@code refTypeId}.
 */
@Entity
@Table(name = "krn_ref_type")
@NoArgsConstructor
@Getter
@Setter()
public class RefType {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  @Column(name = "code", unique = true)
  String code;
}
