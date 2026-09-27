package com.atomskills.askshoes.controller;

import com.atomskills.argent.auth.UserAuth;
import com.atomskills.argent.auth.UserAuthRepository;
import com.atomskills.argent.sequence.NumberGenerator;
import com.atomskills.argent.user.User;
import com.atomskills.argent.user.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Временный debug-код для ручной проверки ядра — не часть реального UI AskShoes. Подлежит удалению.
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestRestController {
  private final UserRepository userRepository;
  private final UserAuthRepository userAuthRepository;
  private final PasswordEncoder passwordEncoder;
  private final NumberGenerator numberGenerator;

  /** Создаёт случайного пользователя (без пароля) — проверка {@link UserRepository#save}. */
  @GetMapping("/user")
  public User getMethodName() {
    User newUser = new User();
    newUser.setDisplayName("test" + UUID.randomUUID().toString());
    newUser.setUsername("test" + UUID.randomUUID().toString());
    return userRepository.save(newUser);
  }

  /**
   * Создаёт пользователя {@code tester}/{@code secret123} — тестовые данные для ручной проверки
   * {@code POST /api/auth/login}.
   */
  @GetMapping("/register-user")
  public User registerTestUser() {
    User user = new User();
    user.setUsername("tester");
    user.setDisplayName("Tester");
    user = userRepository.save(user);

    UserAuth userAuth = new UserAuth();
    userAuth.setUserId(user.getId());
    userAuth.setPasswordHash(passwordEncoder.encode("secret123"));
    userAuthRepository.save(userAuth);

    return user;
  }

  /** Возвращает authorities текущего пользователя — проверка {@code JwtAuthenticationConverter}. */
  @GetMapping("/whoami")
  public List<String> whoami() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication.getAuthorities().stream().map(Object::toString).toList();
  }

  /**
   * Список пользователей с пагинацией/сортировкой ({@link Pageable}, разобран Spring MVC из
   * query-параметров {@code page}/{@code size}/{@code sort}) и опциональным фильтром по подстроке в
   * {@code username} — проверка Specification API.
   *
   * @param usernameContains если задан, оставляет только пользователей, чей {@code username}
   *     содержит эту подстроку (регистрозависимо, SQL {@code LIKE})
   * @param pageable страница/размер/сортировка
   * @return страница пользователей с метаданными ({@code totalElements}, {@code totalPages})
   */
  @GetMapping("/users")
  public Page<User> listUsers(
      @RequestParam(required = false) String usernameContains, Pageable pageable) {
    Page<User> page;
    if (usernameContains != null) {
      Specification<User> spec =
          (root, query, cb) -> cb.like(root.get("username"), "%" + usernameContains + "%");
      page = userRepository.findAll(spec, pageable);
    } else {
      page = userRepository.findAll(pageable);
    }
    return page;
  }

  /** Генерирует следующий номер по шаблону — проверка {@link NumberGenerator}. */
  @GetMapping("/number/{code}")
  public String generateNumber(@PathVariable String code) {
    return numberGenerator.generateNumber(code);
  }
}
