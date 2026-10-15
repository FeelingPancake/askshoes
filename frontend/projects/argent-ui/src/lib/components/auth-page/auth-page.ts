import { Component, input, signal } from '@angular/core';
import { Button } from 'primeng/button';
import { AuthLayout } from '../auth-layout/auth-layout';
import { LoginForm } from '../login-form/login-form';
import { RegisterForm } from '../register-form/register-form';

/**
 * Экран входа и регистрации целиком: {@link AuthLayout} с плакатом, формы {@link LoginForm} и
 * {@link RegisterForm} и переключение между ними. Приложение передаёт только брендинг:
 *
 * ```html
 * <argent-auth-page [description]="'Приёмка заказов'">
 *   <span argent-auth-logo class="wordmark">Ask<b>Shoes</b></span>
 * </argent-auth-page>
 * ```
 *
 * Выходов нет: формы сами сохраняют токен в `AuthTokenStore`, и приложение переключается на
 * оболочку по `isAuthenticated()`. Стили логотипа остаются в приложении — переданная через слот
 * разметка принадлежит шаблону, который её написал.
 */
@Component({
  selector: 'argent-auth-page',
  imports: [AuthLayout, Button, LoginForm, RegisterForm],
  templateUrl: './auth-page.html',
  styleUrl: './auth-page.scss',
})
export class AuthPage {
  /** Краткое описание приложения — подпись внизу плаката. */
  readonly description = input<string>();

  /**
   * Показывать ли переход к регистрации. Должно совпадать с настройкой backend
   * `argent.auth.registration.enabled`: при выключенной регистрации форма получит 403.
   */
  readonly allowRegistration = input(true);

  protected readonly mode = signal<'login' | 'register'>('login');
}
