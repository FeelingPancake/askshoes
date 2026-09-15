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
 * Одно конкретное состояние внутри статусной машины, заданной {@link #statusTypeId} (например,
 * {@code "NEW"} для машины с кодом {@code "ORDER_STATUS"}).
 */
@Entity
@Table(name = "krn_status")
@Getter
@Setter
@NoArgsConstructor
public class Status {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  /** Какой статусной машине принадлежит этот статус — {@link StatusType#id}. */
  UUID statusTypeId;

  String code;
  String name;
  Integer sortOrder;
}
