package com.atomskills.askshoes.entity.order;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Работа по изделию ({@code app_order_item_work}). {@code price} присылает менеджер (по умолчанию —
 * цена из справочника {@code SERVICE_TYPE}), {@code total} считает сервис: у гарантийного изделия
 * он {@code 0}.
 */
@Entity
@Table(name = "app_order_item_work")
@Getter
@Setter
@NoArgsConstructor
public class OrderItemWork {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id")
  OrderItem item;

  /** Позиция справочника {@code SERVICE_TYPE}. */
  UUID serviceTypeId;

  BigDecimal price;
  BigDecimal total;
}
