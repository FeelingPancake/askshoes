package com.atomskills.askshoes.controller;

import com.atomskills.askshoes.dto.client.ClientResponse;
import com.atomskills.askshoes.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST клиентов: пока только поиск по телефону для формы приёмки. */
@RestController
@RequestMapping("api/clients")
@RequiredArgsConstructor
public class ClientController {
  private final ClientService clientService;

  /**
   * Клиент по телефону в любом виде.
   *
   * @param phone телефон ({@code +7 (999) 123-45-67}, {@code 8 999 …})
   * @return клиент
   * @throws com.atomskills.argent.error.ValidationErrorsException 400, если телефон неверный
   * @throws jakarta.persistence.EntityNotFoundException 404, если клиента нет
   */
  @GetMapping("by-phone")
  public ClientResponse getByPhone(@RequestParam String phone) {
    return clientService.getByPhone(phone);
  }
}
