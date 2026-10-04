# AskShoes — правила для агентов

## Документация

Обязательно для каждого публичного элемента системы:

- **JavaDoc** на каждом публичном классе, интерфейсе и методе в
`backend/` (и ядро `argent-backend`, и `askshoes-backend`). Минимум:
что делает, значимые `@param`/`@return`, какие исключения кидает и
когда (`@throws`).
- **TSDoc** на каждом экспортируемом классе/функции/компоненте/сервисе в
`frontend/projects/argent-ui` (и по возможности в
`askshoes-frontend`). То же самое: что делает, `@param`, что
возвращает/emit-ит.
- **README.md на каждом Maven-модуле/Angular project'е**
(`argent-backend`, `askshoes-backend`, `argent-ui`,
`askshoes-frontend`, `argent-api`). Коротко: назначение, из чего
состоит, как использовать/подключить.
- **На Java-пакетах** (`com.atomskills.argent.auth`,
`com.atomskills.argent.user` и т.д.) — `package-info.java` с Javadoc,
не README.md — это идиоматичный Java-механизм, который попадает в
сгенерированную документацию (`mvn javadoc:javadoc`), в отличие от
случайного `.md`-файла внутри `src/main/java`.

## Codestyle

- **Backend (Java)**: Google Java Format, через `fmt-maven-plugin` в
корневом `pom.xml` (наследуется всеми модулями). `mvn fmt:format` —
форматирует, `mvn fmt:check` — проверяет без изменений (для CI)
- **Frontend (TypeScript/HTML/SCSS)**: Prettier (форматирование) +
ESLint (`@antfu/eslint-config` поверх `@angular-eslint`, **не**
Airbnb — `eslint-config-airbnb-base` несовместим с ESLint 9+/10,
который тянет современный `ng add`). `npm run format` / `npm run lint`
в `frontend/`.
- Стили компонентов `argent-ui` и `askshoes-frontend` — SCSS, не голый CSS.

## Архитектурные границы

- **`argent-backend`/`argent-ui`** (ядро) не знают о предметной области
AskShoes. Это касается не только Java/TS-кода, но и **Flyway-миграций**:
структура ядра (таблицы `krn_*`) — в `argent-backend/.../db/migration/`;
доменные/справочные данные конкретного продукта (коды справочников,
тестовые записи и т.п.) — в `askshoes-backend/.../db/migration/`.
- **Нумерация версий Flyway** — две независимые истории: ядро —
`argent-backend/.../db/kernel/` (таблица `krn_schema_history`,
мигрирует первым, см. `flywayMigrationStrategy` в
`ArgentAutoConfiguration`), домен — `askshoes-backend/.../db/migration/`
(`flyway_schema_history`). Номер новой миграции — следующий свободный
**в своей** папке. Номер уже применённой миграции не менять
переименованием файла — Flyway хранит порядок применения в истории,
несовпадение приводит к ошибке валидации или out-of-order.
- Внутри самого ядра — компонент оправдан, только если он универсален  
для любого домена. Прежде чем писать новый компонент в `argent-ui` —  
проверить, нет ли уже готового в PrimeNG 

