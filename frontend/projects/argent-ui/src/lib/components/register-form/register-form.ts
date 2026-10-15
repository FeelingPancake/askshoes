import { Component, inject, output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { Password } from 'primeng/password';
import { applyServerErrors, problemMessage } from '../../errors/problem-detail';
import { AuthApiService } from '../../services/auth-api';
import { FieldError } from '../field-error/field-error';

/**
 * Форма регистрации (логин, отображаемое имя, пароль). Сама вызывает
 * {@link AuthApiService.register}, который сохраняет выданный токен, — после успеха пользователь
 * уже залогинен, родитель только подписывается на {@link registered}.
 *
 * Ограничения полей повторяют `RegisterRequest` на backend: логин и имя до 255 символов, пароль
 * 6–36. Ошибки backend: 400 (логин занят, нарушены ограничения) раскладываются по полям через
 * `applyServerErrors`, остальные (403 «Регистрация отключена», сервер недоступен) — общим
 * сообщением над кнопкой.
 *
 * Пароль — одно поле с кнопкой «показать» (`toggleMask`) вместо поля подтверждения.
 */
@Component({
  selector: 'argent-register-form',
  imports: [Button, FieldError, InputText, Message, Password, ReactiveFormsModule],
  templateUrl: './register-form.html',
  styleUrl: './register-form.scss',
})
export class RegisterForm {
  private readonly authApi = inject(AuthApiService);
  /** Эмитится после успешной регистрации (токен уже сохранён к этому моменту). */
  readonly registered = output<void>();
  protected readonly errorMessage = signal<string | undefined>(undefined);
  protected readonly isSubmitting = signal(false);
  protected readonly form = new FormGroup({
    username: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(255)],
    }),
    displayName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(255)],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(6), Validators.maxLength(36)],
    }),
  });

  /** Обработчик сабмита формы (`(ngSubmit)`). Не делает ничего, если форма невалидна. */
  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage.set(undefined);
    this.isSubmitting.set(true);

    const { username, displayName, password } = this.form.getRawValue();

    this.authApi.register(username, displayName, password).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.registered.emit();
      },
      error: (error: unknown) => {
        this.isSubmitting.set(false);
        if (applyServerErrors(this.form, error)) {
          return;
        }
        this.errorMessage.set(problemMessage(error, 'Не удалось зарегистрироваться'));
      },
    });
  }
}
