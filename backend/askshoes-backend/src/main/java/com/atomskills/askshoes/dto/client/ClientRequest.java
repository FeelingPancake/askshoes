package com.atomskills.askshoes.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Данные клиента в составе запроса заказа.
 *
 * @param fullName ФИО
 * @param phone телефон в любом виде ({@code +7 (999) 123-45-67}, {@code 8 999 …}) — нормализуется
 *     сервисом
 * @param address адрес
 * @param login логин в мессенджере
 * @param sourceId позиция {@code CLIENT_SOURCE}
 * @param statusId позиция {@code CLIENT_STATUS}
 * @param contactMethodIds позиции {@code CONTACT_METHOD}; отсутствие — пустой список, повторы
 *     схлопываются
 */
public record ClientRequest(
    @NotBlank String fullName,
    @NotBlank String phone,
    String address,
    String login,
    UUID sourceId,
    UUID statusId,
    List<@NotNull UUID> contactMethodIds) {

  /** Отсутствующий {@code contactMethodIds} трактуется как пустой. */
  public ClientRequest {
    contactMethodIds = contactMethodIds == null ? List.of() : contactMethodIds;
  }
}
