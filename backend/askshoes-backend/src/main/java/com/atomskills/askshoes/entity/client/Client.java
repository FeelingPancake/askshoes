package com.atomskills.askshoes.entity.client;

import com.atomskills.askshoes.utils.PhoneUtils;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Клиент ({@code app_client}). {@code phone} хранится нормализованным — только цифры, {@code
 * 7XXXXXXXXXX} (см. {@link PhoneUtils#normalizePhone}), и уникален.
 */
@Entity
@Table(name = "app_client")
@Getter
@Setter
@NoArgsConstructor
public class Client {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  String fullName;
  String phone;
  String address;
  String login;

  /** Откуда пришёл клиент — позиция справочника {@code CLIENT_SOURCE}. */
  UUID sourceId;

  /** Статус клиента — позиция справочника {@code CLIENT_STATUS}, задаёт скидку. */
  UUID statusId;

  Instant createdAt;

  @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
  @Setter(AccessLevel.NONE)
  List<ClientContactMethod> contactMethods = new ArrayList<>();
}
