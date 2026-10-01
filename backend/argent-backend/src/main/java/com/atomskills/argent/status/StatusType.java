package com.atomskills.argent.status;

import com.atomskills.argent.reference.entity.RefType;
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
 * Дискриминатор статусной машины (например, {@code "ORDER_STATUS"}, {@code "ITEM_STATUS"}) —
 * отличает наборы статусов/переходов разных сущностей друг от друга. Аналог {@link RefType} в
 * справочниках.
 */
@Entity
@Table(name = "krn_status_type")
@Getter
@Setter
@NoArgsConstructor
public class StatusType {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  String code;
}
