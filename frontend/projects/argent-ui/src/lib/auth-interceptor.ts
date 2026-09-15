import type { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthTokenStore } from './services/auth-token-store';

/**
 * HTTP-интерсептор: если в {@link AuthTokenStore} есть токен, добавляет заголовок
 * `Authorization: Bearer <token>` ко всем исходящим запросам.
 *
 * Не подключается автоматически — приложение обязано зарегистрировать его явно:
 * `provideHttpClient(withInterceptors([authInterceptor]))` в `app.config.ts`.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStore = inject(AuthTokenStore);
  const token = tokenStore.token();

  if (!token) {
    return next(req);
  }

  const authorizedReq = req.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  });

  return next(authorizedReq);
};
