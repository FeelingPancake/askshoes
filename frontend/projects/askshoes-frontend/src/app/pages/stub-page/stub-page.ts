import { Component, input } from '@angular/core';

/**
 * Страница-заглушка для ещё не реализованных разделов — чтобы навигацию можно
 * было прокликать. Заголовок приходит из `data.title` маршрута через
 * `withComponentInputBinding()`.
 */
@Component({
  selector: 'app-stub-page',
  templateUrl: './stub-page.html',
})
export class StubPage {
  /** Заголовок раздела (из `data` маршрута). */
  readonly title = input<string>();
}
