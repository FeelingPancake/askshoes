import { Component } from '@angular/core';
import { AppShell } from 'argent-ui';
import { ASKSHOES_NAV } from './navigation';

/**
 * Корневой компонент: {@link AppShell} ядра (вход ↔ рабочая область) с разделами AskShoes
 * ({@link ASKSHOES_NAV}), словесным знаком и подписью экрана входа.
 */
@Component({
  selector: 'app-root',
  imports: [AppShell],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  /** Разделы верхней навигации. */
  protected readonly nav = ASKSHOES_NAV;

  /** Подпись на плакате экрана входа. */
  protected readonly description =
    'Приёмка, производство и выдача заказов мастерской ремонта обуви и сумок';
}
