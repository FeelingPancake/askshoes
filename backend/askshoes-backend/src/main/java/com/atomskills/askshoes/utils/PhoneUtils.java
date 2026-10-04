package com.atomskills.askshoes.utils;

import com.atomskills.argent.error.ValidationErrorsException;
import java.util.Map;
import lombok.experimental.UtilityClass;

/** Нормализация телефонов к виду {@code 7XXXXXXXXXX}. */
@UtilityClass
public class PhoneUtils {
  public final String INVALID_PHONE_MESSAGE = "Телефон должен содержать 10 или 11 цифр";

  /**
   * Приводит телефон к виду {@code 7XXXXXXXXXX}: оставляет только цифры, ведущую {@code 8} у 11
   * цифр меняет на {@code 7}, к 10 цифрам дописывает {@code 7}.
   *
   * @param raw телефон в любом виде
   * @return нормализованный телефон или {@code null}, если привести нельзя
   */
  public String normalizePhone(String raw) {
    if (raw == null) {
      return null;
    }
    String digits = raw.replaceAll("\\D", "");
    if (digits.length() == 10) {
      return "7" + digits;
    }
    if (digits.length() == 11 && digits.startsWith("8")) {
      return "7" + digits.substring(1);
    }
    if (digits.length() == 11 && digits.startsWith("7")) {
      return digits;
    }
    return null;
  }

  /**
   * То же, что {@link #normalizePhone(String)}, но неверный телефон — ошибка валидации.
   *
   * @param raw телефон в любом виде
   * @param fieldPath путь поля для ошибки ({@code phone}, {@code client.phone})
   * @return нормализованный телефон
   * @throws ValidationErrorsException если телефон нельзя нормализовать
   */
  public String normalizePhoneOrFail(String raw, String fieldPath) {
    String phone = normalizePhone(raw);
    if (phone == null) {
      throw new ValidationErrorsException(Map.of(fieldPath, INVALID_PHONE_MESSAGE));
    }
    return phone;
  }
}
