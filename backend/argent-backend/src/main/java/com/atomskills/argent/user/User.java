package com.atomskills.argent.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Пользователь ядра ({@code krn_user}). Суррогатный {@code uuid}-PK, {@code username} уникален.
 *
 * <p>Не хранит пароль/сессии — см. {@link com.atomskills.argent.auth.UserAuth} и {@link
 * com.atomskills.argent.auth.UserSession}.
 */
@Entity
@Table(name = "krn_user")
@NoArgsConstructor
@Getter
@Setter()
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  String username;

  @Column(name = "display_name")
  String displayName;

  @Column(name = "created_at")
  Instant createdAt;

  /** Проставляет {@code createdAt}, если не задан явно — SQL {@code DEFAULT} JPA игнорирует. */
  @PrePersist
  public void prePersist() {
    createdAt = Instant.now();
  }
}
