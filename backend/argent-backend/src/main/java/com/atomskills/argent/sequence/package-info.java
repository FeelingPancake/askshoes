/**
 * Генерация номеров документов по шаблону ({@code krn_sequence}) с автосбросом счётчика по периоду
 * ({@link ResetPeriod}).
 *
 * <p>Инкремент атомарный — один {@code UPDATE ... RETURNING} с {@code CASE} внутри (см. {@link
 * NumberGenerator})
 */
package com.atomskills.argent.sequence;
