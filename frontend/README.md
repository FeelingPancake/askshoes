# frontend

Angular-workspace AskShoes: ядро `argent-ui`, сгенерированный клиент API `argent-api` и
приложение `askshoes-frontend`.

| Project | Что это | README |
|---|---|---|
| `projects/argent-api` | клиент backend API, **сгенерирован** openapi-generator — руками не правится | `projects/argent-api/README.md` |
| `projects/argent-ui` | переиспользуемое ядро: вход, интерцепторы, каркас, `RefCrudPage` | `projects/argent-ui/README.md` |
| `projects/askshoes-frontend` | приложение AskShoes | `projects/askshoes-frontend/README.md` |

Приложение импортирует библиотеки из **`dist/`** (`tsconfig.json` → `paths`), поэтому после
изменения библиотеки или перегенерации клиента нужен `npm run build-libs`.

## Скрипты

| Команда | Что делает |
|---|---|
| `npm start` | `ng serve askshoes-frontend` — http://localhost:4200 |
| `npm run watch` | пересобирает `argent-ui` в `dist/` при каждом сохранении (второй терминал рядом со `start`) |
| `npm run build-libs` | собирает `argent-api` и `argent-ui` |
| `npm run build` | `build-libs` + production-сборка приложения |
| `npm run generate-api` | качает `/v3/api-docs` с запущенного backend (`fetch-openapi-spec`) и перегенерирует `argent-api` |
| `npm run lint` / `lint:fix` | ESLint (`argent-ui`, `askshoes-frontend`) |
| `npm run format` | Prettier по `projects/` |
| `npm test` | Vitest (тесты отложены до конца roadmap) |

## Обычный цикл

1. Backend запущен на `:8080`, Postgres — `docker compose up -d` из корня репозитория.
2. Изменился API → `npm run generate-api` → `npm run build-libs`.
3. `npm start` (+ `npm run watch`, если правишь `argent-ui`).
