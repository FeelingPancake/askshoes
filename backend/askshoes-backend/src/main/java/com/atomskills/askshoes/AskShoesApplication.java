package com.atomskills.askshoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа приложения AskShoes — предметная реализация поверх переиспользуемого ядра {@code
 * argent-backend} (подключено как обычная Maven-зависимость).
 */
@SpringBootApplication
public class AskShoesApplication {

  public static void main(String[] args) {
    SpringApplication.run(AskShoesApplication.class, args);
  }
}
