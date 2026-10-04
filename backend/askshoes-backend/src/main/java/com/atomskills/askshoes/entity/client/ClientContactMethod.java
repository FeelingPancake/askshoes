package com.atomskills.askshoes.entity.client;

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

/** Предпочтительный способ связи клиента ({@code app_client_contact_method}). */
@Entity
@Table(name = "app_client_contact_method")
@Getter
@Setter
@NoArgsConstructor
public class ClientContactMethod {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id")
  Client client;

  /** Позиция справочника {@code CONTACT_METHOD}. */
  UUID contactMethodId;
}
