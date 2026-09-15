# askshoes-backend

Предметная реализация AskShoes поверх ядра `argent-backend`.

## Что внутри

- **`com.atomskills.askshoes`** — `AskShoesApplication` (`@SpringBootApplication`).
- **`com.atomskills.askshoes.controller`** — временный debug-код (`TestRestController`) для
  ручной проверки ядра. Подлежит удалению.
- **`db/migration/`** — доменные миграции AskShoes (начинаются с `V100` — см. `AGENTS.md`,
  раздел про нумерацию версий Flyway). Только структурные/справочные вещи, контролируемые кодом
  (например, сами **типы** справочников `krn_ref_type` — `ITEM_CATEGORY`, `BRAND` и т.д.) —
  конкретные **позиции** справочников (`krn_ref_item`) сюда не идут, заводятся через
  `POST /api/refs/{code}`, не миграцией.

## Запуск

Нужен PostgreSQL (см. `docker-compose.yml` в корне репозитория — `docker compose up -d`, порт
`5432`). `src/main/resources/application.yml` настроен на `localhost:5432/postgres`.

```
mvn clean install
java -jar target/askshoes-backend-1.0-SNAPSHOT.jar
```

Backend поднимается на `localhost:8080`.
