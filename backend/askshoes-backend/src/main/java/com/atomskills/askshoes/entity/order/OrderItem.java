package com.atomskills.askshoes.entity.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Изделие в заказе ({@code app_order_item}). Ссылки на справочники — {@code UUID} позиций {@code
 * krn_ref_item}. Уникального ключа на ({@code order_id}, {@code seq_no}) нет: он падал бы на
 * промежуточном состоянии при перестановке изделий.
 */
@Entity
@Table(name = "app_order_item")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  Order order;

  /** Порядковый номер изделия в заказе, 1..N — задаётся порядком в запросе. */
  Integer seqNo;

  UUID categoryId;
  UUID itemTypeId;
  UUID brandId;
  Boolean warranty = false;
  UUID colorId;
  UUID materialId;
  UUID sizeId;

  /** Износ, 0–100 %. */
  Integer wearPercent;

  String comment;
  BigDecimal worksTotal = BigDecimal.ZERO;

  @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
  @Setter(AccessLevel.NONE)
  List<OrderItemWork> works = new ArrayList<>();

  @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
  @Setter(AccessLevel.NONE)
  List<OrderItemDefect> defects = new ArrayList<>();
}
