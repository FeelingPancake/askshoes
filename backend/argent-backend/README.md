# argent-backend

Переиспользуемое backend-ядро на Spring Boot. Не знает о предметной области — только общие для
любого домена вещи.

## Что внутри

- **`com.atomskills.argent`** — `ArgentAutoConfiguration` (точка входа, autoconfigure),
  `ArgentExceptionHandler` (единый формат ошибок, RFC 7807); `ValidationErrorsException` — ручная
  валидация с ответом 400 в том же формате, что и `@Valid`.
- **`com.atomskills.argent.user`** — пользователи ядра (`krn_user`).
- **`com.atomskills.argent.auth`** — пароли, сессии, выдача JWT (`krn_user_auth`,
  `krn_user_session`, `POST /api/auth/login`).
- **`com.atomskills.argent.security`** — роли (`krn_role`, `krn_user_role`), проверка JWT на
  каждом запросе (default-deny).
- **`com.atomskills.argent.reference`** — универсальный справочник (`krn_ref_type`,
  `krn_ref_item`), один REST-контроллер `/api/refs/{code}` (GET/POST/PUT/DELETE) на любое
  количество плоских справочников; `POST /api/refs/import` — импорт многих справочников одним
  XLSX/JSON-файлом (upsert по `code`, всё или ничего).
- **`com.atomskills.argent.sequence`** — генерация номеров документов по шаблону (`krn_sequence`)
  с атомарным инкрементом/сбросом счётчика (`NumberGenerator`).
- **`com.atomskills.argent.audit`** — автоматический журнал изменений любой JPA-сущности
  (`krn_audit_entry`).
- **`com.atomskills.argent.status`** — конечный автомат как данные (`krn_status_type`,
  `krn_status`, `krn_status_transition`), проверка перехода — `StatusTransitionService`.
- **`com.atomskills.argent.attachment`** — вложения произвольной сущности (`krn_attachment`,
  файлы на ФС, `/api/attachments`).
- **`com.atomskills.argent.transfer`** — экспорт XLSX/CSV/PDF/DOCX и импорт XLSX/CSV
  (`/api/export/{format}`, `/api/import/{format}`).

Подробности по каждому пакету — в `package-info.java` внутри него.

## Документация API

`GET /v3/api-docs` — OpenAPI-спецификация (JSON), `/swagger-ui.html` — Swagger UI. Оба открыты
без аутентификации (осознанно, см. `securityFilterChain`). Angular-клиент `argent-api`
генерируется из спецификации на стороне фронтенда: `npm run generate-api` в `frontend/`
(backend должен быть запущен).

## Как подключить

Обычная Maven-зависимость:

```xml
<dependency>
    <groupId>com.atomskills</groupId>
    <artifactId>argent-backend</artifactId>
    <version>${project.version}</version>
</dependency>
```

Автоконфигурация подхватывается сама — ничего явно импортировать не нужно. Приложению-потребителю
нужно только настроить `DataSource` и `argent.storage.root-path` (Flyway-миграции ядра —
`db/migration/V1__...` … `V8__...` — уже лежат внутри jar-а и применятся автоматически).

**Важно**: `argent-backend` содержит только миграции **структуры** ядра (таблицы `krn_*`) — никогда
доменных/бизнес-данных (конкретные коды справочников, тестовые записи и т.п.). Такие миграции —
дело приложения-потребителя (`askshoes-backend`), у него своя папка `db/migration/`, своя нумерация
версий продолжает общую последовательность (Flyway не различает, из какого jar пришла миграция,
собирает единую историю по всему classpath).

## Как устроено

**Безопасность.** `/api/auth/login` открыт, всё остальное требует валидного Bearer JWT с
неотозванной сессией (`revoked` проверяется на каждом запросе). Роли пользователя подгружаются в
`Authentication` как `ROLE_<name>`.

**Списки.** Фильтры и пагинация — `Specification` + `Pageable` из Spring Data JPA прямо в
контроллере (образец — `ReferenceController.list`). Общего базового класса CRUD нет.

**Нумерация.** Генерация атомарна — проверено под параллельной нагрузкой (10 одновременных
запросов → 10 разных последовательных номеров). REST для создания самих `krn_sequence` нет —
заводятся миграцией, как и `krn_ref_type`.

**Аудит.** Нативные Hibernate event listeners (`PostInsert`/`PostUpdate`/`PostDelete`),
зарегистрированные через `Integrator`/`HibernatePropertiesCustomizer` (см. Javadoc
`AuditEventListener`). Доменному коду ничего вызывать не нужно. Запись идёт напрямую через
`JdbcTemplate`, в обход Hibernate-сессии — иначе вставка внутри незавершённого flush ломает
очередь действий Hibernate.

**Статусы.** `StatusTransitionService.assertTransitionAllowed(statusTypeId, from, to)` проверяет,
существует ли переход (`from = null` — начальный переход при создании сущности) и есть ли у
текущего пользователя роль, если переход её требует. Сам переход выполняет вызывающий код.

**Вложения.** Файлы — на ФС под `argent.storage.root-path`, в БД только метаданные и
сгенерированный `storage_key` (не совпадает с именем файла — защита от коллизий и path traversal).
Привязка к сущности — пара `entity_type`/`entity_id`, как в аудите.

**Экспорт/импорт.** Единый контракт: `columns` (порядок и состав столбцов) + `rows`. Импорт
возвращает `List<Map<String, String>>` — все значения строками, приведение типов делает вызывающий
код. PDF и DOCX — только экспорт.

## Известные ограничения

- `ArgentExceptionHandler` превращает любое исключение в 500, включая неверный пароль,
  «не найдено» и «нет доступа».
- `StatusTransitionService` не зарегистрирован как бин в `ArgentAutoConfiguration` — приложение
  его не увидит.
- PDF строится стандартным шрифтом Times-Roman без кириллицы и выводится построчным текстом, без
  таблицы.
- RSA-ключ для JWT генерируется при каждом старте — после перезапуска все токены недействительны.
- CORS жёстко настроен на `http://localhost:4200`.
- `/api/test/**` открыт без токена — временно, пока нет seed-миграции с первым администратором.
- `ReferenceController.update`/`delete` не проверяют, что `itemId` принадлежит справочнику с
  указанным `code`.
- `AttachmentController` не проверяет права на конкретную сущность и доверяет `Content-Type`
  клиента (частично смягчено `Content-Disposition: attachment` и `nosniff`).
