/**
 * Роли и права доступа: default-deny цепочка Spring Security, проверка JWT (подпись, срок действия,
 * {@code revoked}-сессия) и подгрузка ролей в {@link
 * org.springframework.security.core.Authentication}.
 *
 * <p>Отдельно от {@link com.argent.kernel.auth} — там логин/выдача токена, здесь — что происходит с
 * каждым последующим запросом, уже несущим токен.
 */
package com.argent.kernel.security;
