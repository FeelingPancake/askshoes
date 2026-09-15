import type { ApplicationConfig } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideApi } from 'argent-api';
import { ARGENT_API_BASE_URL, authInterceptor } from 'argent-ui';
import { providePrimeNG } from 'primeng/config';

import { routes } from './app.routes';
import { AskShoesPreset } from './theme/askshoes-preset';

const API_BASE_URL = 'http://localhost:8080';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([authInterceptor])),
    providePrimeNG({ theme: { preset: AskShoesPreset, options: { darkModeSelector: false } } }),
    { provide: ARGENT_API_BASE_URL, useValue: API_BASE_URL },
    provideApi(API_BASE_URL),
  ],
};
