import type { MenuItem } from 'primeng/api';
import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthLayout, AuthTokenStore, LoginForm, ShellLayout } from 'argent-ui';
import { ButtonModule } from 'primeng/button';
import { MenuModule } from 'primeng/menu';
import { ASKSHOES_NAV } from './navigation';

/**
 * Корневой компонент. Показывает экран входа ({@link AuthLayout} + {@link LoginForm}) или (после входа) {@link ShellLayout} с реальными
 * экранами внутри, в зависимости от {@link AuthTokenStore.isAuthenticated}. Разделы AskShoes
 * ({@link ASKSHOES_NAV}), словесный знак и меню пользователя передаются в {@link ShellLayout}
 * снаружи — сам layout переиспользуем из `argent-ui`, не переписан заново для этого приложения.
 */
@Component({
  selector: 'app-root',
  imports: [AuthLayout, LoginForm, RouterOutlet, ShellLayout, ButtonModule, MenuModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly tokenStore = inject(AuthTokenStore);

  /** Разделы верхней навигации. */
  protected readonly nav = ASKSHOES_NAV;

  /** Пункты всплывающего меню пользователя. */
  protected readonly userMenu: MenuItem[] = [
    { label: 'Выйти', icon: 'pi pi-sign-out', command: () => this.logout() },
  ];
  protected description = 'Приёмка, производство и выдача заказов мастерской ремонта обуви и сумок';

  /** Вызывается после успешного логина. */
  onLoggedIn(): void {}

  /** Разлогинивает пользователя — пункт «Выйти» меню пользователя. */
  logout(): void {
    this.tokenStore.clear();
  }
}
