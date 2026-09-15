package com.atomskills.argent.auth;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data репозиторий для {@link UserAuth}. */
public interface UserAuthRepository extends JpaRepository<UserAuth, UUID> {

  /**
   * Ищет запись авторизации по id пользователя (связь 1:1).
   *
   * @param userId id пользователя ({@link com.atomskills.argent.user.User#id})
   * @return запись с хешем пароля, если у пользователя вообще настроена авторизация по паролю
   */
  Optional<UserAuth> findByUserId(UUID userId);
}
