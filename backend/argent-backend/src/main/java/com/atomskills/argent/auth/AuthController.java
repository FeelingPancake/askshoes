package com.atomskills.argent.auth;

import com.atomskills.argent.user.User;
import com.atomskills.argent.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Точки входа модуля аутентификации: {@code POST /api/auth/login} и {@code POST
 * /api/auth/register}.
 */
@RestController
public class AuthController {
  private final UserRepository userRepository;
  private final UserAuthRepository userAuthRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;
  private final RegistrationService registrationService;

  public AuthController(
      UserRepository userRepository,
      UserAuthRepository userAuthRepository,
      PasswordEncoder passwordEncoder,
      TokenService tokenService,
      RegistrationService registrationService) {
    this.userRepository = userRepository;
    this.userAuthRepository = userAuthRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenService = tokenService;
    this.registrationService = registrationService;
  }

  /**
   * Проверяет логин/пароль и выдаёт токен через {@link TokenService#issue(User)}.
   *
   * @param request логин и пароль
   * @return токен для заголовка {@code Authorization: Bearer <token>}
   * @throws org.springframework.security.authentication.BadCredentialsException если пользователь
   *     не найден, у него не настроен пароль, или пароль неверный
   */
  @Operation(operationId = "login")
  @PostMapping("api/auth/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    User user =
        userRepository
            .findByUsername(request.username())
            .orElseThrow(() -> new BadCredentialsException("User not found"));
    UserAuth userAuth =
        userAuthRepository
            .findByUserId(user.getId())
            .orElseThrow(() -> new BadCredentialsException("User auth not found"));
    if (!passwordEncoder.matches(request.password(), userAuth.getPasswordHash())) {
      throw new BadCredentialsException("Invalid username or password");
    }
    String token = tokenService.issue(user);

    return new LoginResponse(token);
  }

  /**
   * Регистрирует пользователя и сразу выдаёт токен — повторный логин не нужен. Подробности (роли,
   * транзакция) — {@link RegistrationService#register(RegisterRequest)}.
   *
   * <p>Путь открыт в {@code securityFilterChain} ({@code permitAll}) всегда; закрыта ли
   * регистрация, решает сервис по {@code argent.auth.registration.enabled}.
   *
   * @param registerRequest логин, отображаемое имя, пароль
   * @return токен для заголовка {@code Authorization: Bearer <token>}; статус 201
   * @throws org.springframework.security.access.AccessDeniedException если регистрация выключена
   *     (403)
   * @throws com.atomskills.argent.error.ValidationErrorsException если логин занят (400)
   */
  @Operation(operationId = "register")
  @PostMapping("api/auth/register")
  @ResponseStatus(HttpStatus.CREATED)
  public LoginResponse register(@Valid @RequestBody RegisterRequest registerRequest) {
    return new LoginResponse(registrationService.register(registerRequest));
  }
}
