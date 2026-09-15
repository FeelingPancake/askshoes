import type { ArgentNavItem } from './nav-item';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DrawerModule } from 'primeng/drawer';
import { TabsModule } from 'primeng/tabs';
import { filter, fromEvent, map } from 'rxjs';

/** Путь из URL роутера без query-параметров (`?…`) и фрагмента (`#…`). */
function cleanPath(url: string): string {
  return url.split(/[?#]/, 1)[0];
}

/**
 * Совпадает ли путь с маршрутом пункта: точное равенство или вложенный путь.
 * Проверка `route + '/'` (а не просто `startsWith(route)`) нужна, чтобы
 * `/orders` не считался активным для `/orders-archive`.
 */
function matches(path: string, route: string): boolean {
  return path === route || path.startsWith(`${route}/`);
}

/** Мобильный режим каркаса: ширина окна меньше 768px. */
const MOBILE_QUERY = '(max-width: 767px)';

/**
 * Общий каркас страницы с верхней двухуровневой навигацией — переиспользуемый
 * между всеми приложениями поверх `argent-ui`.
 * - `[argent-shell-brand]` — логотип/название приложения;
 * - `[argent-shell-user]` — блок пользователя (меню профиля, «Выйти»);
 * - по умолчанию — контент страницы (обычно `<router-outlet />`).
 *
 * @example
 * ```html
 * <argent-shell-layout [nav]="nav">
 *   <span argent-shell-brand>Моё приложение</span>
 *   <p-button argent-shell-user icon="pi pi-user" [text]="true" ariaLabel="Меню пользователя" />
 *   <router-outlet />
 * </argent-shell-layout>
 * ```
 */
@Component({
  selector: 'argent-shell-layout',
  imports: [TabsModule, ButtonModule, DrawerModule, RouterLink],
  templateUrl: './shell-layout.html',
  styleUrl: './shell-layout.scss',
})
export class ShellLayout {
  /** Пункты навигации приложения (два уровня). */
  readonly nav = input.required<ArgentNavItem[]>();

  private readonly router = inject(Router);

  /**
   * Текущий путь без `?…`/`#…`. Берётся `urlAfterRedirects`: для `/orders`
   * `url` остался бы `/orders`, а реально открыт `/orders/reception` — по
   * нему и должна подсвечиваться вторая строка.
   */
  private readonly path = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map((e) => cleanPath(e.urlAfterRedirects)),
    ),
    { initialValue: cleanPath(this.router.url) },
  );

  /** Активный раздел (первая строка) или `undefined`, если URL вне меню. */
  protected readonly activeTop = computed(() =>
    this.nav().find((item) => matches(this.path(), item.route)),
  );

  /** Активный подраздел (вторая строка) среди детей активного раздела. */
  protected readonly activeSub = computed(() =>
    this.activeTop()?.children?.find((item) => matches(this.path(), item.route)),
  );

  private readonly mobileQuery = matchMedia(MOBILE_QUERY);

  /** Мобильный режим (`< 768px`): бургер со шторкой вместо первой строки вкладок. */
  protected readonly isMobile = toSignal(
    fromEvent(this.mobileQuery, 'change').pipe(map(() => this.mobileQuery.matches)),
    { initialValue: this.mobileQuery.matches },
  );

  /** Видимость мобильной шторки навигации (бургер-меню). */
  protected readonly drawerOpen = signal(false);

  constructor() {
    // Шторка — только мобильная навигация: при расширении окна закрываем её,
    // иначе она осталась бы открытой поверх полноценной первой строки вкладок.
    effect(() => {
      if (!this.isMobile()) {
        this.drawerOpen.set(false);
      }
    });
  }

  /**
   * Переход по выбранной вкладке (мышь или клавиатура). `p-tabs` отдаёт
   * `undefined`, если значение сброшено, — такой выбор игнорируется.
   */
  protected navigate(route: string | number | undefined): void {
    if (route === undefined || route === null) {
      return;
    }
    void this.router.navigateByUrl(String(route));
  }

  /** Активен ли пункт меню (раздел или подраздел) — для подсветки в шторке. */
  protected isActive(route: string): boolean {
    return this.activeTop()?.route === route || this.activeSub()?.route === route;
  }
}
