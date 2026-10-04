package com.atomskills.askshoes.service;

import com.atomskills.argent.error.ValidationErrorsException;
import com.atomskills.askshoes.dto.client.ClientRequest;
import com.atomskills.askshoes.dto.client.ClientResponse;
import com.atomskills.askshoes.entity.client.Client;
import com.atomskills.askshoes.entity.client.ClientContactMethod;
import com.atomskills.askshoes.repository.ClientRepository;
import com.atomskills.askshoes.utils.PhoneUtils;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Поиск клиента по телефону и его upsert при сохранении заказа. */
@Service
@RequiredArgsConstructor
public class ClientService {
  private final ClientRepository clientRepository;

  /**
   * Клиент по телефону.
   *
   * @param rawPhone телефон в любом виде
   * @return найденный клиент
   * @throws ValidationErrorsException если телефон нельзя нормализовать (ключ {@code phone})
   * @throws EntityNotFoundException если клиента с таким телефоном нет
   */
  @Transactional(readOnly = true)
  public ClientResponse getByPhone(String rawPhone) {
    String phone = PhoneUtils.normalizePhoneOrFail(rawPhone, "phone");
    return clientRepository
        .findByPhone(phone)
        .map(ClientResponse::from)
        .orElseThrow(() -> new EntityNotFoundException("Клиент не найден: " + phone));
  }

  /**
   * Находит клиента по телефону или создаёт нового, затем перезаписывает его поля присланными.
   * Способы связи синхронизируются по разнице, а не {@code clear()} + {@code add()}: Hibernate при
   * flush выполняет INSERT раньше DELETE, и повторно добавленный способ нарушил бы unique.
   *
   * <p>Ссылки на справочники здесь не проверяются — это делает вызывающий до вызова.
   *
   * @param request данные клиента
   * @return сохранённый (managed) клиент
   * @throws ValidationErrorsException если телефон нельзя нормализовать (ключ {@code client.phone})
   */
  @Transactional
  public Client upsertByPhone(ClientRequest request) {
    String phone = PhoneUtils.normalizePhoneOrFail(request.phone(), "client.phone");
    Client client =
        clientRepository
            .findByPhone(phone)
            .orElseGet(
                () -> {
                  Client created = new Client();
                  created.setPhone(phone);
                  created.setCreatedAt(Instant.now());
                  return created;
                });

    client.setFullName(request.fullName());
    client.setAddress(request.address());
    client.setLogin(request.login());
    client.setSourceId(request.sourceId());
    client.setStatusId(request.statusId());
    syncContactMethods(client, new LinkedHashSet<>(request.contactMethodIds()));

    return clientRepository.save(client);
  }

  private void syncContactMethods(Client client, Set<UUID> wanted) {
    client.getContactMethods().removeIf(method -> !wanted.contains(method.getContactMethodId()));

    Set<UUID> present =
        client.getContactMethods().stream()
            .map(ClientContactMethod::getContactMethodId)
            .collect(Collectors.toSet());
    for (UUID contactMethodId : wanted) {
      if (!present.contains(contactMethodId)) {
        ClientContactMethod method = new ClientContactMethod();
        method.setClient(client);
        method.setContactMethodId(contactMethodId);
        client.getContactMethods().add(method);
      }
    }
  }
}
