import type { MenuItem } from 'primeng/api';
import type { ArgentNavItem } from '../shell-layout/nav-item';
import { Component, inject, input } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MenuModule } from 'primeng/menu';
import { AuthTokenStore } from '../../services/auth-token-store';
import { AuthPage } from '../auth-page/auth-page';
import { ShellLayout } from '../shell-layout/shell-layout';

/**
 * Корень приложения на ядре: до входа — {@link AuthPage}, после — {@link ShellLayout} с
 * `<router-outlet />` и меню пользователя («Выйти»). Переключение — по
 * {@link AuthTokenStore.isAuthenticated}.
 *
 * Приложение передаёт только навигацию, тексты и брендинг слотами:
 *
 * ```html
 * <argent-app-shell [nav]="nav" description="Приёмка заказов">
 *   <span argent-shell-brand class="wordmark">AskShoes</span>
 *   <span argent-auth-logo class="wordmark wordmark--poster">Ask<br />Shoes</span>
 * </argent-app-shell>
 * ```
 *
 * Слоты прокидываются во вложенные компоненты через `ngProjectAs`; стили логотипов остаются в
 * приложении, как и у {@link AuthPage}.
 */
@Component({
  selector: 'argent-app-shell',
  imports: [AuthPage, ButtonModule, MenuModule, RouterOutlet, ShellLayout],
  templateUrl: './app-shell.html',
})
export class AppShell {
  /** Разделы верхней навигации (два уровня). */
  readonly nav = input.required<ArgentNavItem[]>();

  /** Подпись на плакате экрана входа. */
  readonly description = input<string>();

  /** Показывать ли регистрацию — см. {@link AuthPage.allowRegistration}. */
  readonly allowRegistration = input(true);

  protected readonly tokenStore = inject(AuthTokenStore);

  /** Пункты всплывающего меню пользователя. */
  protected readonly userMenu: MenuItem[] = [
    { label: 'Выйти', icon: 'pi pi-sign-out', command: () => this.tokenStore.clear() },
  ];
}
