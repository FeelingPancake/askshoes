import type { Route, Routes } from '@angular/router';
import { StubPage } from './pages/stub-page/stub-page';

/** Маршрут подраздела-заглушки: {@link StubPage} с заголовком из `data`. */
function stub(path: string, title: string): Route {
  return { path, component: StubPage, data: { title } };
}

/**
 * Маршруты AskShoes — по разделам {@link ASKSHOES_NAV}. Родитель раздела с
 * подразделами перенаправляет на первый подраздел. Корень и неизвестные URL
 * ведут на дашборд.
 *
 * `pathMatch: 'full'` у пустого пути обязателен: с `'prefix'` пустой префикс
 * совпадает с любым URL, и редирект сработал бы для всех адресов.
 */
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  stub('dashboard', 'Дашборд'),

  { path: 'orders', pathMatch: 'full', redirectTo: 'orders/reception' },
  stub('orders/reception', 'Приёмка'),
  stub('orders/list', 'Заказы'),
  stub('orders/production', 'Производство'),
  stub('orders/logistics', 'Логистика'),

  { path: 'stock', pathMatch: 'full', redirectTo: 'stock/balance' },
  stub('stock/balance', 'Остатки'),
  stub('stock/chemistry', 'Химия'),

  { path: 'clients', pathMatch: 'full', redirectTo: 'clients/list' },
  stub('clients/list', 'Клиенты'),
  stub('clients/claims', 'Претензии'),

  stub('reports', 'Отчёты'),

  { path: 'settings', pathMatch: 'full', redirectTo: 'settings/refs' },
  { path: 'settings/refs', pathMatch: 'full', redirectTo: 'settings/refs/COLOR' },
  {
    path: 'settings/refs/:code',
    loadComponent: async () => import('argent-ui/crud').then((m) => m.RefCrudPage),
  },
  stub('settings/users', 'Пользователи'),
  stub('settings/integrations', 'Интеграции'),

  { path: '**', redirectTo: 'dashboard' },
];
