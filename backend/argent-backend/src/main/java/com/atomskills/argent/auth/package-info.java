/**
 * Аутентификация: пароли, сессии, выдача JWT, регистрация.
 *
 * <p>Гибридная схема: JWT (RS256, {@link org.springframework.security.oauth2.jwt.JwtEncoder}) даёт
 * stateless-проверку подписи/срока действия без похода в БД, а строка в {@code krn_user_session}
 * ({@link com.atomskills.argent.auth.UserSession}) даёт возможность мгновенно отозвать сессию
 * ({@code revoked}) — то, что чистый JWT сам по себе не умеет.
 *
 * <p>{@link com.atomskills.argent.auth.AuthController} — точки входа {@code POST /api/auth/login} и
 * {@code POST /api/auth/register}. Токен в обоих случаях выдаёт {@link
 * com.atomskills.argent.auth.TokenService}. Регистрация выключена по умолчанию и включается
 * настройками {@link com.atomskills.argent.auth.RegistrationProperties} ({@code
 * argent.auth.registration.*}); seed-миграции первого пользователя нет — его создаёт регистрация.
 */
package com.atomskills.argent.auth;
