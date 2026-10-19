import type { ApplicationConfig } from '@angular/core';
import { provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideArgent } from 'argent-ui';

import { routes } from './app.routes';
import { AskShoesPreset } from './theme/askshoes-preset';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideArgent({ apiBaseUrl: 'http://localhost:8080', preset: AskShoesPreset }),
  ],
};
