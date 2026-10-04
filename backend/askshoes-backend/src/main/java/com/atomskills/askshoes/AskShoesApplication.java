package com.atomskills.askshoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Точка входа приложения AskShoes — предметная реализация поверх переиспользуемого ядра {@code
 * argent-backend} (подключено как обычная Maven-зависимость).
 */
@SpringBootApplication
@EntityScan
@EnableJpaRepositories
public class AskShoesApplication {

  public static void main(String[] args) {
    SpringApplication.run(AskShoesApplication.class, args);
  }
}
