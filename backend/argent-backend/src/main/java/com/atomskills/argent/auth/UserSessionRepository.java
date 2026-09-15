package com.atomskills.argent.auth;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data репозиторий для {@link UserSession}. */
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

  /**
   * Ищет сессию по id пользователя. У пользователя может быть несколько сессий одновременно (в
   * отличие от {@link UserAuth}) — метод возвращает произвольную одну, не обязательно последнюю
   * активную; для реального использования (проверка текущей сессии) стоит искать по id самой сессии
   * ({@code sessionId} из JWT-claims), а не по пользователю.
   *
   * @param userId id пользователя
   * @return одна из сессий пользователя, если есть
   */
  Optional<UserSession> findByUserId(UUID userId);
}
