import type { ArgentNavItem } from 'argent-ui';

/**
 * Разделы AskShoes для верхней навигации {@link ShellLayout}: первая строка —
 * разделы, вторая — подразделы активного раздела. Маршруты должны совпадать
 * с `app.routes.ts`. Названия подразделов черновые.
 */
export const ASKSHOES_NAV: ArgentNavItem[] = [
  { label: 'Дашборд', route: '/dashboard' },
  {
    label: 'Заказы',
    route: '/orders',
    children: [
      { label: 'Приёмка', route: '/orders/reception' },
      { label: 'Заказы', route: '/orders/list' },
      { label: 'Производство', route: '/orders/production' },
      { label: 'Логистика', route: '/orders/logistics' },
    ],
  },
  {
    label: 'Склад',
    route: '/stock',
    children: [
      { label: 'Остатки', route: '/stock/balance' },
      { label: 'Химия', route: '/stock/chemistry' },
    ],
  },
  {
    label: 'Клиенты',
    route: '/clients',
    children: [
      { label: 'Клиенты', route: '/clients/list' },
      { label: 'Претензии', route: '/clients/claims' },
    ],
  },
  { label: 'Отчёты', route: '/reports' },
  {
    label: 'Настройки',
    route: '/settings',
    children: [
      { label: 'Справочники', route: '/settings/refs' },
      { label: 'Пользователи', route: '/settings/users' },
      { label: 'Интеграции', route: '/settings/integrations' },
    ],
  },
];
