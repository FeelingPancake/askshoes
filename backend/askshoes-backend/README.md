# askshoes-backend

Предметная реализация AskShoes поверх ядра `argent-backend`.

## Что внутри

- **`com.atomskills.askshoes`** — `AskShoesApplication` (`@SpringBootApplication`). Несёт свои
  `@EntityScan` и `@EnableJpaRepositories`: ядро объявляет их на `com.atomskills.argent`, после
  чего Boot перестаёт сам искать сущности и репозитории в пакете приложения.
- **`com.atomskills.askshoes.client`** — клиенты (`app_client`), уникальны по телефону
  (`7XXXXXXXXXX`). `ClientService.upsertByPhone` вызывается при сохранении заказа;
  `GET /api/clients/by-phone?phone=` — поиск для формы приёмки.
- **`com.atomskills.askshoes.order`** — приёмка заказа как один агрегат: `POST`/`PUT`/`GET
  /api/orders[/{id}]`, список `GET /api/orders?search=`. `OrderService` в одной транзакции
  проверяет запрос (все ошибки сразу, `ValidationErrorsException` → 400), находит/создаёт
  клиента, выдаёт номер `MSK-yyyyMMdd-0000`, синхронизирует изделия/работы/повреждения и
  пересчитывает суммы.
- **`db/migration/`** — доменные миграции AskShoes, своя история Flyway (`flyway_schema_history`;
  ядро — `db/kernel` / `krn_schema_history`). `V100` — первые типы справочников, `V101` —
  таблицы клиента и заказа, `V102` — типы справочников заказа, последовательность `ORDER`,
  статусная машина `ORDER`. Только структурные/справочные вещи, контролируемые кодом
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
