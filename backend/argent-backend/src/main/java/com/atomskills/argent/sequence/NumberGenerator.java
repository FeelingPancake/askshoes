package com.atomskills.argent.sequence;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Атомарная генерация следующего номера документа по {@link Sequence#pattern}.
 *
 * <p>Инкремент/сброс счётчика — один {@code UPDATE ... RETURNING} через {@link JdbcTemplate} (в
 * обход JPA/Hibernate целиком), не read-modify-write в несколько шагов — так исключается гонка при
 * параллельных запросах на один и тот же {@code code}
 */
@RequiredArgsConstructor
public class NumberGenerator {
  private final SequenceRepository sequenceRepository;
  private final JdbcTemplate jdbcTemplate;

  /**
   * Атомарно увеличивает счётчик (или сбрасывает на 1, если наступил новый период — см. {@link
   * ResetPeriod}) и форматирует итоговый номер по {@link Sequence#pattern} (в стиле {@link
   * String#format}: {@code %s} — период, {@code %d}/{@code %06d} — счётчик).
   *
   * @param code код последовательности (например {@code "ORDER_NUMBER"})
   * @return готовый номер документа
   * @throws IllegalArgumentException если последовательности с таким {@code code} нет
   */
  public String generateNumber(String code) {
    Sequence sequence =
        sequenceRepository
            .findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Unknown sequence: " + code));

    String periodKey = computePeriodKey(sequence.getResetPeriod());

    String sql =
        "UPDATE krn_sequence "
            + "SET counter  = CASE WHEN period_key = ? THEN counter + 1 else 1 end, "
            + "period_key = ? "
            + "where code = ? "
            + "RETURNING counter";

    Long counter = jdbcTemplate.queryForObject(sql, Long.class, periodKey, periodKey, code);

    return String.format(sequence.getPattern(), periodKey, counter);
  }

  /**
   * Вычисляет "ключ периода" на основе текущей даты — строку, которая меняется ровно тогда, когда
   * должен сброситься счётчик (для {@code DAILY} — {@code 20261001}, для {@code YEARLY} — год, для
   * {@code NEVER} — постоянная строка, никогда не меняется, значит счётчик никогда не
   * сбрасывается).
   */
  private String computePeriodKey(ResetPeriod resetPeriod) {
    if (resetPeriod == null) {
      throw new IllegalArgumentException("Reset period must not be null");
    }

    LocalDate date = LocalDate.now();
    return switch (resetPeriod) {
      case DAILY -> date.format(DateTimeFormatter.BASIC_ISO_DATE);
      case WEEKLY -> date.getYear() + "-W" + date.getDayOfYear() / 7;
      case MONTHLY -> String.format("%04d-%02d", date.getYear(), date.getMonthValue());
      case YEARLY -> String.valueOf(date.getYear());
      case NEVER -> "1";
      default -> {
        throw new IllegalArgumentException("Unsupported reset period: " + resetPeriod);
      }
    };
  }
}
