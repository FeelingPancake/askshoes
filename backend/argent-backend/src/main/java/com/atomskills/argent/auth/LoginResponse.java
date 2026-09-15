package com.atomskills.argent.auth;

/**
 * Ответ успешного {@code POST /api/auth/login}.
 *
 * @param token подписанный JWT — использовать в заголовке {@code Authorization: Bearer <token>} для
 *     последующих запросов
 */
public record LoginResponse(String token) {}
