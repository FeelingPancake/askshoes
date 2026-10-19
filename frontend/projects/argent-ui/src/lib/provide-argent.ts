import type { HttpInterceptorFn } from '@angular/common/http';
import type { EnvironmentProviders, Provider } from '@angular/core';
import type { Preset } from '@primeuix/themes/types';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { makeEnvironmentProviders } from '@angular/core';
import { provideApi } from 'argent-api';
import { providePrimeNG } from 'primeng/config';
import { authInterceptor } from './auth-interceptor';
import { errorInterceptor } from './errors/error-interceptor';
import { ArgentPreset } from './theme/argent-preset';

/** Настройки {@link provideArgent}. */
export interface ArgentConfig {
  /** Адрес backend, например `http://localhost:8080`. Единственное место, где он задаётся. */
  apiBaseUrl: string;

  /** Пресет темы PrimeNG; по умолчанию нейтральный {@link ArgentPreset}. */
  preset?: Preset;

  /**
   * Дополнительные HTTP-интерсепторы приложения — выполняются после `authInterceptor` и
   * `errorInterceptor`. Отдельный `provideHttpClient` в приложении конфликтовал бы с этим.
   */
  interceptors?: HttpInterceptorFn[];
}

/**
 * Вся обвязка приложения на ядре одним вызовом: `HttpClient` с интерсепторами авторизации и
 * ошибок, клиент `argent-api` с адресом backend и PrimeNG с темой.
 *
 * Маршруты остаются за приложением:
 *
 * ```ts
 * providers: [
 *   provideBrowserGlobalErrorListeners(),
 *   provideRouter(routes, withComponentInputBinding()),
 *   provideArgent({ apiBaseUrl: 'http://localhost:8080', preset: AskShoesPreset }),
 * ]
 * ```
 */
export function provideArgent(config: ArgentConfig): EnvironmentProviders {
  const providers: (Provider | EnvironmentProviders)[] = [
    provideHttpClient(
      withInterceptors([authInterceptor, errorInterceptor, ...(config.interceptors ?? [])]),
    ),
    provideApi(config.apiBaseUrl),
    providePrimeNG({
      theme: { preset: config.preset ?? ArgentPreset, options: { darkModeSelector: false } },
    }),
  ];
  return makeEnvironmentProviders(providers);
}
