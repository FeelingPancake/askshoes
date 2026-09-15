/**
 * Конечные автоматы (state machines) как данные — {@code krn_status_type}/{@code krn_status}/{@code
 * krn_status_transition}.
 *
 * <p>Один generic-движок обслуживает любое число разных статусных машин (заказ, изделие и т.д.),
 * различая их через {@code status_type_id} — тот же приём, что и в {@link
 * com.atomskills.argent.reference} для справочников. Домен ничего не знает о конкретных статусах и
 * переходах в коде: набор допустимых переходов и требуемая для перехода роль хранятся как данные в
 * {@code krn_status_transition}, а не зашиты в {@code if}/{@code switch}.
 *
 * <p>{@link com.atomskills.argent.status.StatusTransitionService#assertTransitionAllowed} —
 * единственная точка проверки "разрешён ли переход X→Y для данной статусной машины, и есть ли у
 * текущего пользователя нужная роль". {@code fromStatusId == null} обозначает начальный переход — в
 * какой статус можно создать сущность сразу, без предыдущего статуса.
 */
package com.atomskills.argent.status;
