package com.atomskills.askshoes.dto.client;

import com.atomskills.askshoes.entity.client.Client;
import com.atomskills.askshoes.entity.client.ClientContactMethod;
import java.util.List;
import java.util.UUID;

/**
 * Клиент в ответах API.
 *
 * @param id id клиента
 * @param fullName ФИО
 * @param phone нормализованный телефон
 * @param address адрес
 * @param login логин в мессенджере
 * @param sourceId позиция {@code CLIENT_SOURCE}
 * @param statusId позиция {@code CLIENT_STATUS}
 * @param contactMethodIds позиции {@code CONTACT_METHOD}
 */
public record ClientResponse(
    UUID id,
    String fullName,
    String phone,
    String address,
    String login,
    UUID sourceId,
    UUID statusId,
    List<UUID> contactMethodIds) {

  /**
   * @param client клиент (коллекция способов связи должна быть доступна — вызывать в транзакции)
   * @return DTO клиента
   */
  public static ClientResponse from(Client client) {
    return new ClientResponse(
        client.getId(),
        client.getFullName(),
        client.getPhone(),
        client.getAddress(),
        client.getLogin(),
        client.getSourceId(),
        client.getStatusId(),
        client.getContactMethods().stream().map(ClientContactMethod::getContactMethodId).toList());
  }
}
