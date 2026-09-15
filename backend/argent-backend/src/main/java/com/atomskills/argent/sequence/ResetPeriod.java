package com.atomskills.argent.sequence;

/**
 * Как часто сбрасывается счётчик последовательности ({@link Sequence#resetPeriod}). Определяет, что
 * именно {@link NumberGenerator} считает "новым периодом" — а значит, когда счётчик начинается
 * заново с 1, а не продолжает расти.
 */
public enum ResetPeriod {
  NEVER,
  YEARLY,
  MONTHLY,
  WEEKLY,
  DAILY
}
