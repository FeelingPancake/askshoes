# argent-api

Angular-клиент, **сгенерированный** из OpenAPI-спецификации backend'а (`openapi-generator-cli`,
генератор `typescript-angular`) — не редактируется руками.

## Как перегенерировать

1. Поднять backend (`backend/askshoes-backend`, порт `8080`).
2. Из `frontend/`: `npm run generate-api` — скачивает `/v3/api-docs` в `openapi.json` и
   перегенерирует всё содержимое `src/lib/`.

Файлы `src/public-api.ts`, `ng-package.json`, `package.json`, `tsconfig.*.json` на верхнем уровне
пакета — ручные (обвязка Angular-библиотеки), их генератор не трогает.

## Использование

`provideApi(baseUrl)` из этого пакета — в `app.config.ts`, рядом с `ARGENT_API_BASE_URL` из
`argent-ui` (два разных токена под один и тот же адрес — генератор не знает про токен `argent-ui`).
Сгенерированные сервисы (`*ControllerService`, например `ReferenceControllerService`) —
`providedIn: 'root'`, используют обычный `HttpClient`, поэтому `authInterceptor` из `argent-ui`
подставляет JWT автоматически, без дополнительной настройки.

## Известное ограничение

Сеть у Java-процесса `openapi-generator-cli` может ломаться локальными VPN/прокси-клиентами
в TUN-режиме (перехват трафика на уровне драйвера, включая `127.0.0.1`) — если `npm run
generate-api` падает с `BindException: Cannot assign requested address`, это оно. Обход уже
встроен в скрипт: `fetch-openapi-spec` качает спецификацию `curl`'ом (другой сетевой стек, не
подвержен) в локальный файл, генератор читает файл, а не URL напрямую.
