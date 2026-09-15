/**
 * Автоматический журнал изменений — таблица {@code krn_audit_entry}.
 *
 * <p>Записи создаются не вручную, а перехватом событий Hibernate ({@code
 * PostInsertEventListener}/{@code PostUpdateEventListener}/{@code PostDeleteEventListener})
 */
package com.atomskills.argent.audit;
