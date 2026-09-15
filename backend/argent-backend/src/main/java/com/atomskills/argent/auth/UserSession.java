package com.atomskills.argent.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Сессия ({@code krn_user_session}), созданная при успешном логине ({@link AuthController}).
 *
 * <p>Даёт возможность отозвать доступ ({@code revoked}) до истечения {@code expiresAt}.
 */
@Entity
@Table(name = "krn_user_session")
@Getter
@Setter
public class UserSession {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  @Column(name = "user_id", nullable = false)
  UUID userId;

  @Column(name = "revoked", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
  Boolean revoked;

  @Column(
      name = "created_at",
      nullable = false,
      columnDefinition = "TIMESTAMPTZ DEFAULT current_timestamp")
  Instant createdAt;

  @Column(name = "expires_at", nullable = false, columnDefinition = "TIMESTAMPTZ NOT NULL")
  Instant expiresAt;

  /**
   * Заполняет поля со значениями по умолчанию, если не заданы явно — SQL {@code DEFAULT} JPA
   * игнорирует (Hibernate всегда явно перечисляет колонки в {@code INSERT}).
   */
  @PrePersist
  public void prePersist() {
    if (revoked == null) {
      revoked = false;
    }

    if (createdAt == null) {
      createdAt = Instant.now();
    }
    if (expiresAt == null) {
      expiresAt = createdAt.plus(Duration.ofDays(30)); // Срок жизни сессии по умолчанию — 30 дней
    }
  }
}
