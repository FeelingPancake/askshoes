package com.atomskills.argent.sequence;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Конфигурация одной последовательности номеров ({@code krn_sequence}) — например {@code
 * "ORDER_NUMBER"}. Сам счётчик ({@code counter}/{@code periodKey}) мутируется только атомарно,
 * через {@link NumberGenerator}
 */
@Entity
@Table(name = "krn_sequence")
@Getter
@Setter
@NoArgsConstructor
public class Sequence {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  String code;
  String pattern;

  @Enumerated(EnumType.STRING)
  ResetPeriod resetPeriod;

  String periodKey;
  Long counter;
}
