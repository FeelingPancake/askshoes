import type { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { ARGENT_API_BASE_URL } from '../tokens/api-base-url';
import { AuthTokenStore } from './auth-token-store';

interface LoginRequest {
  username: string;
  password: string;
}

interface LoginResponse {
  token: string;
}

/** HTTP-клиент к `POST /api/auth/login` на {@link ARGENT_API_BASE_URL}. */
@Injectable({
  providedIn: 'root',
})
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(ARGENT_API_BASE_URL);
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
    return this.http
      .post<LoginResponse>(`${this.baseUrl}/api/auth/login`, body)
      .pipe(tap((response) => this.tokenStore.setToken(response.token)));
  }
}
