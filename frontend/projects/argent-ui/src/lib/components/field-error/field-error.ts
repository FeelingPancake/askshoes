import type { AbstractControl } from '@angular/forms';
import { Component, input } from '@angular/core';
import { Message } from 'primeng/message';

/** Ошибки, которые умеет показывать {@link FieldError}: встроенные `Validators.*` + `server`. */
interface KnownErrors {
  server?: string;
  required?: true;
  maxlength?: { requiredLength: number };
  minlength?: { requiredLength: number };
  min?: { min: number };
  max?: { max: number };
  email?: true;
}

/**
 * Сообщение об ошибке под полем формы. Показывает первую ошибку {@link control}, когда поле
 * уже тронуто: клиентские валидаторы (`required`, `maxlength`, ...) и ошибку `server`, которую
 * кладёт `applyServerErrors` из ответа backend.
 *
 * Использование: `<argent-field-error [control]="form.controls.name" />`.
 */
@Component({
  selector: 'argent-field-error',
  imports: [Message],
  templateUrl: './field-error.html',
})
export class FieldError {
  readonly control = input.required<AbstractControl>();

  /** Текст первой ошибки контрола или `undefined`, если показывать нечего. */
  protected message(): string | undefined {
    const control = this.control();
    const errors = control.errors as KnownErrors | null;
    if (!errors || !control.touched) {
      return undefined;
    }

    if (errors.server) {
      return errors.server;
    }
    if (errors.required) {
      return 'Обязательное поле';
    }
    if (errors.maxlength) {
      return `Не больше ${errors.maxlength.requiredLength} символов`;
    }
    if (errors.minlength) {
      return `Не меньше ${errors.minlength.requiredLength} символов`;
    }
    if (errors.min) {
      return `Не меньше ${errors.min.min}`;
    }
    if (errors.max) {
      return `Не больше ${errors.max.max}`;
    }
    if (errors.email) {
      return 'Некорректный email';
    }
    return 'Некорректное значение';
  }
}
