package com.atomskills.askshoes.entity.order;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Повреждение изделия ({@code app_order_item_defect}), уникально в пределах изделия. */
@Entity
@Table(name = "app_order_item_defect")
@Getter
@Setter
@NoArgsConstructor
public class OrderItemDefect {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id")
  OrderItem item;

  /** Позиция справочника {@code DEFECT_TYPE}. */
  UUID defectTypeId;
}
