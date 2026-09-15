package com.atomskills.argent.audit;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Перехватывает {@code INSERT}/{@code UPDATE}/{@code DELETE} любой сущности Hibernate и пишет
 * записи в {@code krn_audit_entry} — регистрируется не как {@code @Component} (Hibernate ничего не
 * знает про Spring), а через {@code Integrator}, см. {@link
 * com.atomskills.argent.ArgentAutoConfiguration}.
 *
 * <p><b>Почему запись идёт через {@link JdbcTemplate}, а не через {@link AuditEntryRepository}</b>:
 * эти listener'ы вызываются во время ещё не завершённого flush текущей Hibernate-сессии — если
 * внутри них сохранять сущность через ту же сессию (например, {@code entityManager.persist(...)}),
 * можно получить {@code ConcurrentModificationException} из очереди действий Hibernate (мы
 * добавляем новую операцию в очередь, которая в этот момент сама итерируется на выполнение). Прямой
 * INSERT через JDBC полностью обходит Hibernate-сессию, поэтому такой проблемы нет — тот же приём,
 * что и в {@link com.atomskills.argent.sequence.NumberGenerator}.
 */
public class AuditEventListener
    implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

  private static final String INSERT_SQL =
      "INSERT INTO krn_audit_entry "
          + "(entity_type, entity_id, action, field, old_value, new_value, actor_id, actor_name, created_at) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private final JdbcTemplate jdbcTemplate;

  public AuditEventListener(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void onPostInsert(PostInsertEvent event) {
    // На INSERT/DELETE не расписываем поля по отдельности — фиксируем сам факт создания/удаления
    // сущности целиком (см. package-info пакета audit).
    write(event.getEntity(), event.getId(), "INSERT", null, null, null);
  }

  @Override
  public void onPostUpdate(PostUpdateEvent event) {
    String[] propertyNames = event.getPersister().getPropertyNames();
    Object[] oldState = event.getOldState();
    Object[] newState = event.getState();
    // getDirtyProperties() — индексы реально изменившихся полей; Hibernate уже посчитал разницу
    // сам, повторно сравнивать oldState/newState по всем полям не нужно.
    for (int index : event.getDirtyProperties()) {
      String field = propertyNames[index];
      Object oldValue = oldState == null ? null : oldState[index];
      Object newValue = newState == null ? null : newState[index];
      write(
          event.getEntity(),
          event.getId(),
          "UPDATE",
          field,
          oldValue == null ? null : oldValue.toString(),
          newValue == null ? null : newValue.toString());
    }
  }

  @Override
  public void onPostDelete(PostDeleteEvent event) {
    write(event.getEntity(), event.getId(), "DELETE", null, null, null);
  }

  @Override
  public boolean requiresPostCommitHandling(EntityPersister persister) {
    // false = вызывать сразу после выполнения SQL-операции (в рамках ещё не закоммиченной
    // транзакции), не дожидаясь коммита. Если бы операция вставки аудита сама могла откатиться
    // независимо от основной транзакции, тут был бы смысл ждать коммит — но запись аудита идёт в
    // той же транзакции и тем же JDBC-соединением, откатится вместе с основным изменением.
    return false;
  }

  private void write(
      Object entity, Object id, String action, String field, String oldValue, String newValue) {
    if (entity instanceof AuditEntry) {
      // Защита от рекурсии на случай, если запись в krn_audit_entry когда-нибудь пойдёт не только
      // через прямой JDBC (как сейчас), но и через AuditEntryRepository/Hibernate.
      return;
    }
    UUID actorId = null;
    String actorName = null;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      try {
        actorId = UUID.fromString(authentication.getName());
      } catch (IllegalArgumentException e) {
        // Authentication есть, но её "имя" — не UUID пользователя (например, анонимная сессия) —
        // тогда просто не знаем, кто автор изменения, это не ошибка.
      }
    }
    jdbcTemplate.update(
        INSERT_SQL,
        entity.getClass().getSimpleName(),
        String.valueOf(id),
        action,
        field,
        oldValue,
        newValue,
        actorId,
        actorName,
        Timestamp.from(Instant.now()));
  }
}
