package com.atomskills.argent.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Тело запроса {@code POST /api/auth/login}.
 *
 * @param username логин пользователя
 * @param password пароль в открытом виде
 */
public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
