import type { LoginRequest, LoginResponse } from 'argent-api';
import type { Observable } from 'rxjs';
import { inject, Injectable } from '@angular/core';
import { AuthControllerService } from 'argent-api';
import { tap } from 'rxjs';
import { AuthTokenStore } from './auth-token-store';

/**
 * Вход и регистрация (`POST /api/auth/login`, `POST /api/auth/register`). Обёртка над
 * сгенерированным `AuthControllerService` из `argent-api`: сохраняет выданный токен в
 * {@link AuthTokenStore}, чтобы формы не делали этого сами. Адрес backend — из `provideApi(...)`.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthApiService {
  private readonly authApi = inject(AuthControllerService);
  private readonly tokenStore = inject(AuthTokenStore);

  /**
   * Логинится и, при успехе, сохраняет полученный токен в {@link AuthTokenStore} — вызывающему
   * коду не нужно делать это самому.
   *
   * @param username логин
   * @param password пароль
   * @returns поток с ответом backend; ошибка (неверные данные) проходит как error-канал Observable
   */
  login(username: string, password: string): Observable<LoginResponse> {
    const body: LoginRequest = { username, password };
    return this.authApi
      .login({ loginRequest: body })
      .pipe(tap((response) => this.tokenStore.setToken(response.token!)));
  }

  /**
   * Регистрирует пользователя и, при успехе, сохраняет выданный токен в {@link AuthTokenStore} —
   * пользователь сразу оказывается залогинен.
   *
   * @param username логин; уникален
   * @param displayName отображаемое имя
   * @param password пароль, 6–36 символов
   * @returns поток с ответом backend; ошибки — в error-канале: 400 (логин занят — `errors.username`,
   *   нарушены ограничения полей), 403 (регистрация выключена в `argent.auth.registration`)
   */
  register(username: string, displayName: string, password: string): Observable<LoginResponse> {
    return this.authApi
      .register({ registerRequest: { username, displayName, password } })
      .pipe(tap((response) => this.tokenStore.setToken(response.token!)));
  }
}
