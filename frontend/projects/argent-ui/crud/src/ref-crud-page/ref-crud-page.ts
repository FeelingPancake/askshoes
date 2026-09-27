import type { RefItemResponse } from 'argent-api';
import type { TableLazyLoadEvent } from 'primeng/table';
import { CommonModule } from '@angular/common';
import { Component, inject, input, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ReferenceControllerService } from 'argent-api';
import { applyServerErrors, FieldError, problemMessage } from 'argent-ui';
import { ConfirmationService, MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { Dialog } from 'primeng/dialog';
import { InputNumber } from 'primeng/inputnumber';
import { InputText } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Toast } from 'primeng/toast';
import { ToggleSwitch } from 'primeng/toggleswitch';
import { Toolbar } from 'primeng/toolbar';

/**
 * Универсальная CRUD-страница для любого справочника, заведённого через {@code argent.reference}
 * (`krn_ref_type`/`krn_ref_item`, backend — `ReferenceController`, `GET/POST/PUT/DELETE
 * /api/refs/{code}`). Один компонент обслуживает любое количество справочников — какой именно,
 * определяет {@link code}, передаваемый снаружи (например, из роутинга).
 *
 * Самодостаточна: сама носит {@link ConfirmDialog}/{@link Toast} в шаблоне и провайдит
 * {@link ConfirmationService}/{@link MessageService} на уровне компонента (у PrimeNG они не
 * `providedIn: 'root'`) — подключающему коду не нужно ничего регистрировать глобально сверх
 * обычного {@code provideApi(...)} для {@code argent-api} (см. {@code app.config.ts}).
 */
@Component({
  selector: 'argent-ref-crud-page',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TableModule,
    Toolbar,
    Button,
    Dialog,
    InputText,
    InputNumber,
    ToggleSwitch,
    Tag,
    ConfirmDialog,
    Toast,
    FieldError,
  ],
  providers: [ConfirmationService, MessageService],
  templateUrl: './ref-crud-page.html',
  styleUrl: './ref-crud-page.scss',
})
export class RefCrudPage {
  private readonly referenceApi = inject(ReferenceControllerService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly messageService = inject(MessageService);

  /** Код типа справочника (`krn_ref_type.code`), например {@code "COLOR"}. */
  readonly code = input.required<string>();
  /** Заголовок страницы; по умолчанию — сам {@link code}. */
  readonly title = input<string>();

  protected readonly items = signal<RefItemResponse[]>([]);
  protected readonly totalRecords = signal(0);
  protected readonly loading = signal(false);
  protected readonly dialogVisible = signal(false);
  protected readonly saving = signal(false);
  protected readonly editingItem = signal<RefItemResponse | null>(null);

  /** Последнее событие `(onLazyLoad)` — нужно, чтобы {@link reload} повторил тот же запрос
   * (страницу/сортировку), а не сбрасывал пользователя на начало списка после save/delete. */
  private lastLazyLoadEvent: TableLazyLoadEvent = { first: 0, rows: 10 };

  protected readonly form = new FormGroup({
    code: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(255)],
    }),
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(255)],
    }),
    active: new FormControl(true, { nonNullable: true }),
    sortOrder: new FormControl(0, { nonNullable: true }),
  });

  /**
   * Обработчик `(onLazyLoad)` таблицы — вызывается PrimeNG и при первой отрисовке, и при каждой
   * смене страницы/сортировки. Строит {@code Pageable}-совместимый запрос из события таблицы и
   * подставляет ответ в {@link items}/{@link totalRecords}.
   */
  onLazyLoad(event: TableLazyLoadEvent): void {
    this.lastLazyLoadEvent = event;
    const rows = event.rows ?? 10;
    const page = Math.floor((event.first ?? 0) / rows);
    const sortField = Array.isArray(event.sortField) ? event.sortField[0] : event.sortField;
    const sort = sortField ? [`${sortField},${event.sortOrder === 1 ? 'asc' : 'desc'}`] : undefined;

    this.loading.set(true);
    this.referenceApi.list(this.code(), { page, size: rows, sort }).subscribe({
      next: (result) => {
        this.items.set(result.content ?? []);
        this.totalRecords.set(result.page?.totalElements ?? 0);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Не удалось загрузить список',
          detail: problemMessage(error, ''),
        });
      },
    });
  }

  /** Открывает диалог создания новой позиции — форма пустая. */
  openCreate(): void {
    this.editingItem.set(null);
    this.form.reset({ code: '', name: '', active: true, sortOrder: 0 });
    this.dialogVisible.set(true);
  }

  /** Открывает диалог редактирования — форма заполнена текущими значениями {@link item}. */
  openEdit(item: RefItemResponse): void {
    this.editingItem.set(item);
    this.form.reset({
      code: item.code ?? '',
      name: item.name ?? '',
      active: item.active ?? true,
      sortOrder: item.sortOrder ?? 0,
    });
    this.dialogVisible.set(true);
  }

  /** Сабмит формы (`(ngSubmit)`) — в зависимости от {@link editingItem} создаёт или обновляет. */
  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const request = this.form.getRawValue();
    const editing = this.editingItem();
    const result$ = editing
      ? this.referenceApi.update(this.code(), editing.id!, request)
      : this.referenceApi.create(this.code(), request);

    result$.subscribe({
      next: () => {
        this.saving.set(false);
        this.dialogVisible.set(false);
        this.reload();
        this.messageService.add({ severity: 'success', summary: 'Сохранено' });
      },
      error: (error: unknown) => {
        this.saving.set(false);
        if (applyServerErrors(this.form, error)) {
          return;
        }
        this.messageService.add({
          severity: 'error',
          summary: 'Не удалось сохранить',
          detail: problemMessage(error, ''),
        });
      },
    });
  }

  confirmDelete(item: RefItemResponse): void {
    this.confirmationService.confirm({
      message: `Удалить "${item.name}"?`,
      header: 'Подтверждение',
      icon: 'pi pi-exclamation-triangle',
      accept: () => this.delete(item),
    });
  }

  private delete(item: RefItemResponse): void {
    this.referenceApi._delete(this.code(), item.id!).subscribe({
      next: () => {
        this.reload();
        this.messageService.add({ severity: 'success', summary: 'Удалено' });
      },
      error: (error: unknown) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Не удалось удалить',
          detail: problemMessage(error, ''),
        });
      },
    });
  }

  /** Перезагружает ту же страницу/сортировку таблицы — используется после save/delete. */
  private reload(): void {
    this.onLazyLoad(this.lastLazyLoadEvent);
  }
}
