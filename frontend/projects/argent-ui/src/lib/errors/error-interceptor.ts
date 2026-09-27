import type { HttpInterceptorFn } from '@angular/common/http';
import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthTokenStore } from '../services/auth-token-store';

/**
 * HTTP-интерсептор ошибок: на 401 (токен истёк или сессия отозвана) стирает токен — приложение
 * реактивно переключается на экран входа через {@link AuthTokenStore.isAuthenticated}.
 *
 * 401 от самого `POST /api/auth/login` (неверный пароль) не трогает — это обычная ошибка формы.
 * Ошибку в любом случае пробрасывает дальше: текст для пользователя показывает компонент
 * (через `problemMessage`/`applyServerErrors`), потому что только он знает контекст.
 *
 * Не подключается автоматически — приложение регистрирует его явно:
 * `provideHttpClient(withInterceptors([authInterceptor, errorInterceptor]))`.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStore = inject(AuthTokenStore);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        !req.url.endsWith('/api/auth/login')
      ) {
        tokenStore.clear();
      }
      return throwError(() => error);
    }),
  );
};
