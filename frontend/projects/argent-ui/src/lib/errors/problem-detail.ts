import type { FormGroup } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

/**
 * Тело ошибки от backend — RFC 7807 `ProblemDetail`, который отдаёт `ArgentExceptionHandler`
 * на любую ошибку любого контроллера.
 */
export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  /** Человекочитаемое описание ошибки — его и показываем пользователю. */
  detail?: string;
  instance?: string;
  /** Только у 400 от `@Valid`: имя поля DTO → сообщение валидатора. */
  errors?: Record<string, string>;
}

/** Достаёт {@link ProblemDetail} из ошибки `HttpClient`, если backend его прислал. */
export function toProblemDetail(error: unknown): ProblemDetail | undefined {
  if (!(error instanceof HttpErrorResponse)) {
    return undefined;
  }
  const body: unknown = error.error;
  return body !== null && typeof body === 'object' ? (body as ProblemDetail) : undefined;
}

/**
 * Текст ошибки для показа пользователю (toast, `p-message`): `detail` из {@link ProblemDetail},
 * а если его нет — {@link fallback}.
 *
 * @param error ошибка из error-канала Observable `HttpClient`
 * @param fallback текст, если backend не прислал `detail` (например, пустое тело у 401)
 */
export function problemMessage(error: unknown, fallback: string): string {
  if (error instanceof HttpErrorResponse && error.status === 0) {
    return 'Сервер недоступен';
  }
  return toProblemDetail(error)?.detail || fallback;
}

/**
 * Раскладывает ошибки валидации backend (`errors` у 400) по контролам формы — как ошибку
 * `server`, которую показывает `argent-field-error`. Ошибка сама снимется, когда пользователь
 * изменит значение поля (Angular перезапустит валидаторы контрола).
 *
 * @param form форма, имена контролов которой совпадают с полями DTO на backend
 * @param error ошибка из error-канала Observable `HttpClient`
 * @returns `true`, если хотя бы одна ошибка легла на поле формы; иначе вызывающему коду
 *   стоит показать общее сообщение через {@link problemMessage}
 */
export function applyServerErrors(form: FormGroup, error: unknown): boolean {
  const errors = toProblemDetail(error)?.errors;
  if (!errors) {
    return false;
  }

  let applied = false;
  for (const [field, message] of Object.entries(errors)) {
    const control = form.get(field);
    if (control) {
      control.setErrors({ server: message });
      control.markAsTouched();
      applied = true;
    }
  }
  return applied;
}
