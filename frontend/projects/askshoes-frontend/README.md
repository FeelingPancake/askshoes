# askshoes-frontend

Предметная реализация AskShoes поверх `argent-ui`.

## Что внутри

- **`App`** (`app-root`) — форма логина / `ShellLayout` из `argent-ui` с
  роутер-outlet, переключается по `AuthTokenStore.isAuthenticated`.
- **`AskShoesPreset`** (`src/app/theme/askshoes-preset.ts`) — тёплый бренд AskShoes поверх
  нейтрального `ArgentPreset` из `argent-ui`: тёплая палитра surface, кирпичный акцент-маркер
  (`{primary.500}` = `#b8471f`), три статуса `p-tag`. Главная кнопка при этом намеренно остаётся
  чернильной (`{surface.900}`), а не кирпичной — кирпич используется только как маркер (активная
  вкладка, фокус, логотип). Подключается в `app.config.ts` через
  `providePrimeNG({ theme: { preset: AskShoesPreset, options: { darkModeSelector: false } } })`.
- **`ASKSHOES_NAV`** (`src/app/navigation.ts`) — конфиг меню `ShellLayout`: разделы верхнего
  уровня и их подразделы (маршруты должны совпадать с `app.routes.ts`).
- **Шрифты** — пакеты `@fontsource*`, подключены в `angular.json` → `build.options.styles`:
  IBM Plex Sans Variable (весь интерфейс, с кириллицей), IBM Plex Mono 400/500 (номера, суммы,
  даты) и Archivo Narrow 700 — только латинский словесный знак «AskShoes» (`app.scss`).
- **`StubPage`** (`src/app/pages/stub-page`) — заглушка для разделов, экраны которых ещё не
  реализованы.

## Запуск

Нужен backend на `localhost:8080` (см. `../../backend/askshoes-backend/README.md`).

```bash
ng serve askshoes-frontend
```

Откроется на `http://localhost:4200`. CORS на backend уже настроен именно под этот адрес
(`CorsConfigurationSource`/`.cors(...)` в `securityFilterChain`, см. `ArgentAutoConfiguration`).

## Статус

Разделы меню (`ASKSHOES_NAV`) и каркас (`ShellLayout` + тема) на месте, но большинство разделов —
пока `StubPage`-заглушки без реального содержимого. Единственный работающий экран с данными —
`/settings/refs` (`RefCrudPage`).

Приложение zoneless (без `zone.js`): в Angular 21 это режим по умолчанию, обнаружение изменений
держится на сигналах. `provideBrowserGlobalErrorListeners()` в `app.config.ts` к zoneless отношения
не имеет — он лишь перехватывает глобальные ошибки браузера (`error`/`unhandledrejection`).
