import type { ApplicationConfig } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideApi } from 'argent-api';
import { authInterceptor, errorInterceptor } from 'argent-ui';
import { providePrimeNG } from 'primeng/config';

import { routes } from './app.routes';
import { AskShoesPreset } from './theme/askshoes-preset';

const API_BASE_URL = 'http://localhost:8080';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
    providePrimeNG({ theme: { preset: AskShoesPreset, options: { darkModeSelector: false } } }),
    provideApi(API_BASE_URL),
  ],
};
