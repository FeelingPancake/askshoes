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
- **Нумерация версий Flyway** едина для всего classpath (ядро +
`askshoes-backend` вместе, Flyway не различает jar). Перед созданием
новой миграции — посмотреть, какой номер уже занят максимальным (в
любом из двух `db/migration/`), и брать следующий свободный. Отдельные
диапазоны номеров под ядро/домен не резервировать — на практике это
требует ручной правки уже применённой `flyway_schema_history` при
каждом отклонении от плана и создаёт больше риска, чем пользы (номер
миграции нельзя сменить простым переименованием файла постфактум —
Flyway хранит порядок применения в `flyway_schema_history`;
несовпадение приводит к ошибке валидации или out-of-order).
- Внутри самого ядра — компонент оправдан, только если он универсален  
для любого домена. Прежде чем писать новый компонент в `argent-ui` —  
проверить, нет ли уже готового в PrimeNG 

