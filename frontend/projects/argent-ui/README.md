# argent-ui

Переиспользуемая библиотека Angular-компонентов. Не знает о
предметной области AskShoes.

**Принцип**: не переизобретать примитивы PrimeNG. Компонент здесь оправдан только если он
составной/оркестрирующий (несколько PrimeNG-компонентов вместе) или доменно-осведомлённый
(знает про backend API, но не про конкретный домен AskShoes). Простые вещи (бейджи, тосты,
таблицы) — использовать `p-*` компоненты PrimeNG напрямую в приложении, без обёртки.

## Что внутри

- **`AuthTokenStore`** — signal-based хранилище JWT-токена, персистентность в `localStorage`.
- **`authInterceptor`** — HTTP-interceptor, добавляет `Authorization: Bearer <token>` к
  исходящим запросам. Нужно зарегистрировать явно (см. ниже).
- **`errorInterceptor`** — на 401 (кроме самого логина) стирает токен, приложение уходит на экран
  входа. Ошибку пробрасывает дальше. Регистрируется явно, после `authInterceptor`.
- **`problemMessage(error, fallback)`** / **`applyServerErrors(form, error)`** — разбор
  `ProblemDetail` от backend: текст для toast и раскладка `errors` (400 от `@Valid`) по полям формы.
- **`FieldError`** (`<argent-field-error [control]="...">`) — сообщение под полем: клиентские
  `Validators.*` и серверная ошибка `server`.
- **`ARGENT_API_BASE_URL`** — `InjectionToken<string>` для адреса backend. Обязателен к
  регистрации приложением-потребителем.
- **`AuthApiService`** — HTTP-клиент к `POST /api/auth/login`.
- **`LoginForm`** (`<argent-login-form>`) — готовая форма логина.
- **`ShellLayout`** (`<argent-shell-layout>`) — каркас страницы с верхней двухуровневой
  навигацией (см. «Каркас страницы» ниже).
- **`ArgentNavItem`** — тип пункта навигации для `ShellLayout.nav`.

## Тема

`ArgentPreset` (`projects/argent-ui/src/lib/theme/argent-preset.ts`) — нейтральный
«Swiss»-пресет PrimeNG, надстроенный поверх стандартного `Aura` через `definePreset`. Задаёт
общие для всех приложений ограничения: нулевые скругления (`primitive.borderRadius.*`),
нейтральный (не цветной) `primary`, сплошная рамка вместо свечения у `focusRing`, рамки вместо
теней у оверлеев, и стиль вкладок навигации `ShellLayout` (`components.tabs`). Ветку
`colorScheme.dark` пресет не переопределяет — тёмная палитра структурно наследуется от `Aura`,
но физически неактивна, пока приложение не включит тёмный режим явно.

Ядро не знает бренда конкретного приложения. Чтобы надстроить бренд (акцентный цвет, тёплая
палитра surface и т. п.), приложение вызывает `definePreset(ArgentPreset, {...})` ещё раз и
переопределяет только нужные семантические токены — ядро при этом не меняется. Пример —
`AskShoesPreset` в `askshoes-frontend` (см. его README). Подключается через
`providePrimeNG({ theme: { preset: ... } })` в `app.config.ts` приложения.

## Глобальные стили

Помимо темы PrimeNG, ядро публикует файл глобальных SCSS-стилей
`projects/argent-ui/src/styles/argent.scss` — CSS-переменные шрифтов, типографическая шкала и
утилитарные классы. Публикуется как ассет пакета (`ng-package.json`), поэтому доступен из
`dist/argent-ui` после `ng build argent-ui`.

Подключение в `angular.json` приложения-потребителя:

```json
"stylePreprocessorOptions": {
  "includePaths": ["dist"]
}
```

и в `styles.scss` приложения:

```scss
@use 'argent-ui/styles/argent';
```

(алиас `argent-ui` резолвится в `dist/argent-ui` через `includePaths`, аналогично `tsconfig`
→ `paths`).

Ядро **не подключает файлы шрифтов** (`.woff2`/CSS пакетов `@fontsource*`) — это ответственность
приложения. `argent.scss` лишь объявляет имена `font-family` с системным fallback, поэтому до
подключения шрифтов страница просто использует fallback, без ошибок. В `askshoes-frontend`
шрифты подключены в `angular.json` → `build.options.styles`:
`@fontsource-variable/ibm-plex-sans/index.css` (UI-шрифт, есть кириллица),
`@fontsource/ibm-plex-mono/400.css`, `@fontsource/ibm-plex-mono/500.css`,
`@fontsource/archivo-narrow/700.css` (только латинский словесный знак приложения).

Переменные:

- `--argent-font-sans` (`IBM Plex Sans Variable`), `--argent-font-mono` (`IBM Plex Mono`) —
  семейства шрифтов.
- `--argent-text-title` (28/700), `--argent-text-section` (18/600), `--argent-text-body`
  (14/400), `--argent-text-small` (12/400), `--argent-text-label` (11/600, капс) —
  типографическая шкала как значения сокращённого свойства `font`.

Базовый кегль 14px задан на `html` (`body` — `1rem`): компоненты PrimeNG считают размеры в `rem`,
поэтому становятся плотными вместе с текстом. Шкала `--argent-text-*` задана в px и от корневого
кегля не зависит.

Утилитарные классы:

- `.argent-page-title` — заголовок страницы.
- `.argent-section-title` — заголовок секции.
- `.argent-meta` — второстепенный/мелкий текст.
- `.argent-label` — метка капсом (только шапки таблиц и группы полей).
- `.argent-mono` — моноширинный текст с табличными цифрами (номера документов, суммы, даты).

Также `argent.scss` даёт `p-tag` рамку 1px `currentColor` (в `Aura` у тега нет токена рамки, а
Swiss-направление требует контур вместо «таблетки»).

## Каркас страницы

`ShellLayout` — переиспользуемый каркас с верхней двухуровневой навигацией: шапка (слот бренда →
первая строка вкладок по `nav` → слот пользователя), под ней вторая строка вкладок с `children`
активного раздела (если у раздела нет `children` — второй строки нет вовсе), ниже — контент.
Единственный источник правды для подсветки активных вкладок — текущий URL (без `?…`/`#…`), а не
локальное состояние, поэтому подсветка переживает перезагрузку страницы на глубоком маршруте.
Мобильный режим (`< 768px`) заменяет первую строку вкладок на кнопку-бургер, открывающую
`p-drawer` с тем же деревом `nav`.

```html
<argent-shell-layout [nav]="nav">
  <span argent-shell-brand>Моё приложение</span>
  <p-button
    argent-shell-user
    icon="pi pi-user"
    [text]="true"
    severity="secondary"
    ariaLabel="Меню пользователя"
  />
  <router-outlet />
</argent-shell-layout>
```

```typescript
import type { ArgentNavItem } from 'argent-ui';

const nav: ArgentNavItem[] = [
  { label: 'Дашборд', route: '/dashboard' },
  {
    label: 'Заказы',
    route: '/orders',
    children: [{ label: 'Приёмка', route: '/orders/reception' }],
  },
];
```

Ядро не знает разделов конкретного приложения — массив `nav` целиком задаёт приложение
(см. `askshoes-frontend/src/app/navigation.ts`).

## Как подключить

`argent-ui` — часть того же Angular workspace, не отдельный npm-пакет (см. `tsconfig.json` →
`paths`) — приложение видит **собранную** библиотеку из `dist/argent-ui`, не исходники.

Разработка с автообновлением — два терминала в `frontend/`:

```bash
npm run watch   # ng build argent-ui --watch — пересобирает dist/ при каждом сохранении
npm start       # ng serve askshoes-frontend — подхватывает новый dist/ и перезагружает страницу
```

Чтобы `ng serve` замечал пересборку `dist/`, библиотеки исключены из пребандлинга Vite
(`angular.json` → `serve.options.prebundle.exclude: ["argent-ui", "argent-api"]`). Без этого
dev-сервер один раз кэширует `argent-ui` и дальше показывает старую версию, сколько её ни
пересобирай. Правки `angular.json` подхватываются только перезапуском `ng serve`.

Разовая сборка без watch — `ng build argent-ui` (или `npm run build-libs` для обеих библиотек).

В `app.config.ts` приложения:

```typescript
import { ARGENT_API_BASE_URL, authInterceptor, errorInterceptor } from 'argent-ui';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

providers: [
  provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
  { provide: ARGENT_API_BASE_URL, useValue: 'http://localhost:8080' },
];
```

## Secondary entry point `argent-ui/crud`

Тяжёлые экраны на PrimeNG Table/Dialog (`RefCrudPage`) лежат в `projects/argent-ui/crud/` и
собираются отдельным модулем. Импортировать их только лениво, иначе они снова попадут в
стартовый бандл:

```typescript
{ path: 'settings/refs', loadComponent: async () => import('argent-ui/crud').then((m) => m.RefCrudPage) }
```

Код внутри `crud/` импортирует общее из `'argent-ui'`, не относительными путями. Новый экран:
папка в `crud/src/` + строка в `crud/src/public-api.ts`.

## Codestyle

Prettier + ESLint (`@antfu/eslint-config` + `angular-eslint`, без конфликтов с Prettier через
`eslint-config-prettier`). `npm run lint` / `npm run format` из `frontend/`.
