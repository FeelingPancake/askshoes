package com.atomskills.argent.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Связка пользователь ↔ роль ({@code krn_user_role}, M:N). {@code 01-decision-surrogate-keys.md};
 * уникальность пары обеспечена {@code UNIQUE}- constraint'ом в миграции, не первичным ключом.
 */
@Table(name = "krn_user_role")
@Entity
@Getter
@Setter
public class UserRole {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  @Column(name = "user_id", nullable = false)
  UUID userId;

  @Column(name = "role_id", nullable = false)
  UUID roleId;
}
