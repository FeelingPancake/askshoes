import { InjectionToken } from '@angular/core';

/**
 * Базовый URL backend (`argent-backend`), например `http://localhost:8080`.
 *
 * Без значения по умолчанию — приложение-потребитель обязано зарегистрировать его само:
 * `{ provide: ARGENT_API_BASE_URL, useValue: '...' }` в `app.config.ts`. Если забыть —
 * Angular при старте выбросит понятную `NullInjectorError`, а не тихо использует неверный URL.
 */
export const ARGENT_API_BASE_URL = new InjectionToken<string>('ARGENT_API_BASE_URL');
