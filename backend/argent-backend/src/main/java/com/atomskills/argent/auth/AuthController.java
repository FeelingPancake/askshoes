package com.atomskills.argent.auth;

import com.atomskills.argent.user.User;
import com.atomskills.argent.user.UserRepository;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Единственная точка входа модуля аутентификации: {@code POST /api/auth/login}. */
@RestController
public class AuthController {
  private final UserRepository userRepository;
  private final UserAuthRepository userAuthRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserSessionRepository userSessionRepository;
  private final JwtEncoder jwtEncoder;

  public AuthController(
      UserRepository userRepository,
      UserAuthRepository userAuthRepository,
      PasswordEncoder passwordEncoder,
      UserSessionRepository userSessionRepository,
      JwtEncoder jwtEncoder) {
    this.userRepository = userRepository;
    this.userAuthRepository = userAuthRepository;
    this.passwordEncoder = passwordEncoder;
    this.userSessionRepository = userSessionRepository;
    this.jwtEncoder = jwtEncoder;
  }

  /**
   * Проверяет логин/пароль, создаёт новую {@link UserSession} (по умолчанию на 30 дней, см. {@link
   * UserSession#prePersist()}) и выдаёт подписанный JWT (claims: {@code sub} — id пользователя,
   * {@code sessionId} — id созданной сессии).
   *
   * @param request логин и пароль
   * @return токен для заголовка {@code Authorization: Bearer <token>}
   * @throws org.springframework.security.authentication.BadCredentialsException если пользователь
   *     не найден, у него не настроен пароль, или пароль неверный
   */
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
    UserSession userSession = new UserSession();
    userSession.setUserId(user.getId());
    userSessionRepository.save(userSession);

    Instant now = Instant.now();

    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("argent")
            .subject(user.getId().toString())
            .issuedAt(now)
            .expiresAt(userSession.getExpiresAt())
            .claim("sessionId", userSession.getId().toString())
            .build();
    JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();

    String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

    return new LoginResponse(token);
  }
}
