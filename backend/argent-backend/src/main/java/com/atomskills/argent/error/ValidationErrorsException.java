package com.atomskills.argent.error;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

/**
 * Ошибки ручной (не {@code @Valid}) валидации — сразу все, а не первая. {@link
 * ArgentExceptionHandler} превращает его в 400 того же формата, что и ошибки {@code @Valid}: {@code
 * detail} = «Ошибка валидации», свойство {@code errors} — эта map. Unchecked — чтобы
 * {@code @Transactional} откатывал транзакцию.
 */
public class ValidationErrorsException extends RuntimeException {
  @Getter private final Map<String, String> errors;

  /**
   * @param errors где ошибка → что не так; копируется с сохранением порядка
   */
  public ValidationErrorsException(Map<String, String> errors) {
    super("Ошибка валидации");
    this.errors = new LinkedHashMap<>(errors);
  }
}
