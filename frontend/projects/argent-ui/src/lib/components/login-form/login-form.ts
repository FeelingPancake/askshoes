import { Component, inject, output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { Password } from 'primeng/password';
import { AuthApiService } from '../../services/auth-api';

/**
 * Форма логина (логин + пароль). Сама делает HTTP-запрос через {@link AuthApiService} и
 * сохраняет токен — родительский компонент только подписывается на {@link loggedIn}, чтобы
 * узнать об успехе (например, для навигации).
 */
@Component({
  selector: 'argent-login-form',
  imports: [ReactiveFormsModule, InputText, Password, Button, Message],
  templateUrl: './login-form.html',
  styleUrl: './login-form.scss',
})
export class LoginForm {
  private readonly authApi = inject(AuthApiService);

  /** Эмитится после успешного логина (токен уже сохранён к этому моменту). */
  readonly loggedIn = output<void>();
  protected readonly errorMessage = signal<string | undefined>(undefined);
  protected readonly isSubmitting = signal(false);

  protected readonly form = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: Validators.required }),
    password: new FormControl('', { nonNullable: true, validators: Validators.required }),
  });

  /** Обработчик сабмита формы (`(ngSubmit)`). Не делает ничего, если форма невалидна. */
  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage.set(undefined);
    this.isSubmitting.set(true);

    const { username, password } = this.form.getRawValue();

    this.authApi.login(username, password).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.loggedIn.emit();
      },
      error: () => {
        this.isSubmitting.set(false);
        this.errorMessage.set('Неверный логин или пароль');
      },
    });
  }
}
