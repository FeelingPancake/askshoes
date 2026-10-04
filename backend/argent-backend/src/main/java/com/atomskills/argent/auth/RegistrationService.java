package com.atomskills.argent.auth;

import com.atomskills.argent.error.ValidationErrorsException;
import com.atomskills.argent.security.Role;
import com.atomskills.argent.security.RoleRepository;
import com.atomskills.argent.security.UserRole;
import com.atomskills.argent.security.UserRoleRepository;
import com.atomskills.argent.user.User;
import com.atomskills.argent.user.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Открытая регистрация пользователя ({@code POST /api/auth/register}). Работает, только если
 * включена {@link RegistrationProperties#enabled()}; по умолчанию выключена.
 */
@Service
@RequiredArgsConstructor
public class RegistrationService {
  private final RegistrationProperties properties;
  private final UserRepository userRepository;
  private final UserAuthRepository userAuthRepository;
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;
  private final TokenService tokenService;

  /**
   * Создаёт пользователя, его пароль (bcrypt), привязывает роли из {@link
   * RegistrationProperties#defaultRoles()} и выдаёт токен через {@link TokenService#issue(User)}.
   * Всё — одной транзакцией: при любой ошибке не остаётся ни пользователя без пароля, ни сессии.
   *
   * @param registerRequest логин, отображаемое имя, пароль
   * @return токен для заголовка {@code Authorization: Bearer <token>}
   * @throws AccessDeniedException если регистрация выключена (403)
   * @throws ValidationErrorsException если логин занят (400, ключ {@code username})
   */
  @Transactional
  public String register(RegisterRequest registerRequest) {
    if (!properties.enabled()) {
      throw new AccessDeniedException("Регистрация отключена");
    }

    if (userRepository.findByUsername(registerRequest.username()).isPresent()) {
      throw new ValidationErrorsException(Map.of("username", "Логин уже занят"));
    }

    User user = new User();
    user.setUsername(registerRequest.username());
    user.setDisplayName(registerRequest.displayName());
    userRepository.save(user);

    UserAuth userAuth = new UserAuth();
    userAuth.setUserId(user.getId());
    userAuth.setPasswordHash(passwordEncoder.encode(registerRequest.password()));
    userAuthRepository.save(userAuth);

    for (String roleName : properties.defaultRoles()) {
      // Seed-миграций ролей нет: роль из default-roles создаётся при первой регистрации.
      // Обратная сторона — опечатка в настройке молча создаст лишнюю роль.
      Role role = roleRepository.findByName(roleName).orElseGet(() -> createRole(roleName));

      UserRole link = new UserRole();
      link.setUserId(user.getId());
      link.setRoleId(role.getId());
      userRoleRepository.save(link);
    }

    return tokenService.issue(user);
  }

  private Role createRole(String roleName) {
    Role role = new Role();
    role.setName(roleName);

    return roleRepository.save(role);
  }
}
