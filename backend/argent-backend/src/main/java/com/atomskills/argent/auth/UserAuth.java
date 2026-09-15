package com.atomskills.argent.auth;

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
 * Пароль пользователя ({@code krn_user_auth}), 1:1 с {@link com.atomskills.argent.user.User}
 * (обеспечено через {@code UNIQUE} на {@code user_id}, PK — свой суррогатный {@code id}).
 *
 * <p>{@code passwordHash} — bcrypt-хеш ({@link
 * org.springframework.security.crypto.password.PasswordEncoder}), никогда не сырой пароль.
 */
@Entity
@Table(name = "krn_user_auth")
@Getter
@Setter
public class UserAuth {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;
}
