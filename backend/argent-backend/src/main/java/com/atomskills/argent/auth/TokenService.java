package com.atomskills.argent.auth;

import com.atomskills.argent.user.User;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Выдача JWT: новая сессия + подписанный токен. */
@Service
@RequiredArgsConstructor
public class TokenService {
  private final UserSessionRepository userSessionRepository;
  private final JwtEncoder jwtEncoder;

  /**
   * Создает новую сессию {@link UserSession} и выдаёт подписанный JWT (claims: {@code sub} — id
   * пользователя, {@code sessionId} — id созданной сессии, {@code username} — логин, для журнала
   * аудита)
   *
   * @param user кому выдается токен
   * @return token
   */
  public String issue(User user) {
    UserSession userSession = new UserSession();
    userSession.setUserId(user.getId());
    userSession = userSessionRepository.save(userSession);
    Instant now = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("argent")
            .subject(user.getId().toString())
            .issuedAt(now)
            .expiresAt(userSession.getExpiresAt())
            .claim("sessionId", userSession.getId().toString())
            .claim("username", user.getUsername())
            .build();
    JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();

    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }
}
