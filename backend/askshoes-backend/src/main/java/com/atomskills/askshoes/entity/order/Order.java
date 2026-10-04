package com.atomskills.askshoes.entity.order;

import com.atomskills.argent.status.Status;
import com.atomskills.askshoes.entity.client.Client;
import com.atomskills.askshoes.service.OrderService;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

/**
 * Заказ ({@code app_order}). Суммы ({@code worksTotal}, {@code discountPercent}, {@code
 * discountAmount}, {@code total}) считает {@link OrderService}, клиент их не присылает.
 *
 * <p>{@code @Table} обязателен: {@code order} — ключевое слово SQL.
 */
@Entity
@Table(name = "app_order")
@Getter
@Setter
@NoArgsConstructor
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  /** Номер вида {@code MSK-20261001-0001} — из {@code krn_sequence} {@code ORDER}. */
  String number;

  Instant acceptedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id")
  Client client;

  /** Кто принял заказ — {@code krn_user.id}. */
  UUID managerId;

  LocalDate dueDate;
  String note;

  /** Статус из статусной машины ядра {@code ORDER}. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "status_id")
  Status status;

  BigDecimal worksTotal = BigDecimal.ZERO;
  BigDecimal discountPercent = BigDecimal.ZERO;
  BigDecimal discountAmount = BigDecimal.ZERO;
  BigDecimal total = BigDecimal.ZERO;
  BigDecimal paidAmount = BigDecimal.ZERO;

  /**
   * Количество изделий — подзапросом в том же {@code SELECT}, чтобы список заказов не грузил
   * коллекцию {@link #items} на каждую строку. Только для чтения.
   */
  @Formula("(select count(*) from app_order_item i where i.order_id = id)")
  @Setter(AccessLevel.NONE)
  Long itemsCount;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("seqNo")
  @Setter(AccessLevel.NONE)
  List<OrderItem> items = new ArrayList<>();
}
