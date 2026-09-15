package com.atomskills.argent.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Spring Data репозиторий для {@link User}. */
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

  /**
   * Ищет пользователя по логину.
   *
   * @param username точное значение {@code username} (уникально)
   * @return пользователь, если найден
   */
  Optional<User> findByUsername(String username);
}
