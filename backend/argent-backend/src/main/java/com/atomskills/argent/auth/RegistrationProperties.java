package com.atomskills.argent.auth;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Настройки открытой регистрации ({@code argent.auth.registration.*}).
 *
 * @param enabled открыт ли {@code POST /api/auth/register}; по умолчанию выключен
 * @param defaultRoles роли нового пользователя — имена из {@code krn_role.name}; по умолчанию нет
 */
@ConfigurationProperties("argent.auth.registration")
public record RegistrationProperties(
    @DefaultValue("false") boolean enabled, @DefaultValue List<String> defaultRoles) {}
