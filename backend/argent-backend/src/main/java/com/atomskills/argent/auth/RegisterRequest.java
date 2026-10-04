package com.atomskills.argent.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Тело запроса {@code POST /api/auth/register}.
 *
 * @param username логин пользователя; уникален, сравнивается с учётом регистра
 * @param displayName отображаемое имя
 * @param password пароль в открытом виде, 6–36 символов; хранится только bcrypt-хеш
 */
public record RegisterRequest(
    @NotBlank @Size(max = 255) String username,
    @NotBlank @Size(max = 255) String displayName,
    @NotBlank @Size(min = 6, max = 36) String password) {}
