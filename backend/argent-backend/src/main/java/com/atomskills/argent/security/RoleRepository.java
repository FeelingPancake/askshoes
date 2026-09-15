package com.atomskills.argent.security;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data репозиторий для {@link Role}. */
public interface RoleRepository extends JpaRepository<Role, UUID> {

  /**
   * Ищет роль по имени.
   *
   * @param name точное имя роли (уникально)
   * @return роль, если существует
   */
  Optional<Role> findByName(String name);
}
