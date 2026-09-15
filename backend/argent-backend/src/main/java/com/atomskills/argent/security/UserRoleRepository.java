package com.atomskills.argent.security;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data репозиторий для {@link UserRole}. */
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

  /**
   * Все назначения ролей конкретному пользователю (у пользователя может быть несколько ролей
   * одновременно — отсюда {@link List}, а не {@link Optional}).
   *
   * @param userId id пользователя
   * @return список связок (для получения имён ролей нужно дополнительно разрешить {@code roleId}
   *     через {@link RoleRepository})
   */
  List<UserRole> findByUserId(UUID userId);
}
