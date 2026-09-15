import { computed, Injectable, signal } from '@angular/core';

const STORAGE_KEY = 'argent.auth.token';

/**
 * Signal-based хранилище JWT-токена сессии, с персистентностью в `localStorage`.
 *
 * `providedIn: 'root'` — единственный экземпляр на всё приложение, доступен через DI без
 * дополнительной регистрации в `app.config.ts` (в отличие от {@link ARGENT_API_BASE_URL} или
 * `authInterceptor`, которые приложение обязано подключить явно).
 *
 * Мутировать токен снаружи нельзя — {@link token} доступен только на чтение
 * (`.asReadonly()`); менять состояние можно только через {@link setToken}/{@link clear}.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthTokenStore {
  private readonly tokenSignal = signal<string | null>(localStorage.getItem(STORAGE_KEY));

  /** Текущий токен (или `null`, если не залогинен). Только для чтения. */
  readonly token = this.tokenSignal.asReadonly();

  /** Производный signal: `true`, если токен есть. */
  readonly isAuthenticated = computed(() => this.tokenSignal() !== null);

  /**
   * Сохраняет токен — и в `localStorage` (переживёт перезагрузку страницы), и в signal
   * (реактивно обновит всё, что читает {@link token}/{@link isAuthenticated}).
   *
   * @param token JWT, полученный от `POST /api/auth/login`
   */
  setToken(token: string): void {
    localStorage.setItem(STORAGE_KEY, token);
    this.tokenSignal.set(token);
  }

  /** Стирает токен (логаут) — и из `localStorage`, и из signal. */
  clear(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.tokenSignal.set(null);
  }
}
