/**
 * Аутентификация: пароли, сессии, выдача JWT.
 *
 * <p>Гибридная схема: JWT (RS256, {@link org.springframework.security.oauth2.jwt.JwtEncoder}) даёт
 * stateless-проверку подписи/срока действия без похода в БД, а строка в {@code krn_user_session}
 * ({@link com.atomskills.argent.auth.UserSession}) даёт возможность мгновенно отозвать сессию
 * ({@code revoked}) — то, что чистый JWT сам по себе не умеет.
 *
 * <p>{@link com.atomskills.argent.auth.AuthController} — единственная точка входа ({@code POST
 * /api/auth/login}).
 */
package com.atomskills.argent.auth;
