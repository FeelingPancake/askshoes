package com.atomskills.argent.status;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Один разрешённый переход {@link #fromStatusId} → {@link #toStatusId} внутри статусной машины
 * {@link #statusTypeId}, опционально ограниченный ролью ({@link #requiredRoleId}). Проверяется
 * через {@link StatusTransitionService#assertTransitionAllowed}.
 */
@Entity
@Table(name = "krn_status_transition")
@Getter
@Setter
@NoArgsConstructor
public class StatusTransition {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  UUID statusTypeId;

  /** {@code null} — начальный переход: в этот статус можно создать сущность сразу. */
  UUID fromStatusId;

  UUID toStatusId;

  /** {@code null} — переход доступен любому аутентифицированному пользователю. */
  UUID requiredRoleId;
}
