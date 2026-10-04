package com.atomskills.askshoes.repository;

import com.atomskills.askshoes.entity.client.Client;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data репозиторий для {@link Client}. */
public interface ClientRepository extends JpaRepository<Client, UUID> {

  /**
   * Ищет клиента по телефону.
   *
   * @param phone уже нормализованный телефон ({@code 7XXXXXXXXXX})
   * @return клиент, если существует
   */
  Optional<Client> findByPhone(String phone);
}
