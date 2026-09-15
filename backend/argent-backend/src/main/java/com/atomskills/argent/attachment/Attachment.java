package com.atomskills.argent.attachment;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Метаданные одного загруженного файла — содержимое лежит на диске, здесь только ссылка на него.
 */
@Entity
@Table(name = "krn_attachment")
@Getter
@Setter
public class Attachment {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  /** К какой сущности домена привязан файл, например {@code "Order"}. */
  String entityType;

  /** id этой сущности (строкой — не обязательно UUID). */
  String entityId;

  /** Оригинальное имя файла, как его прислал клиент — только для отображения/скачивания. */
  String filename;

  String contentType;
  Long sizeBytes;

  /** Имя файла на диске под {@code argent.storage.root-path} — не совпадает с {@link #filename}. */
  String storageKey;

  UUID uploadedBy;
  Instant createdAt;

  @PrePersist
  void onPrePersist() {
    this.createdAt = Instant.now();
  }
}
