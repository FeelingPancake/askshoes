package com.atomskills.argent.attachment;

import java.time.Instant;
import java.util.UUID;

/**
 * Метаданные вложения, отдаваемые наружу через REST — сама сущность {@link Attachment} наружу не
 * идёт (тот же принцип, что {@code RefItemResponse}/{@code LoginResponse}).
 */
public record AttachmentResponse(
    UUID id,
    String entityType,
    String entityId,
    String filename,
    String contentType,
    Long sizeBytes,
    UUID uploadedBy,
    Instant createdAt) {

  static AttachmentResponse from(Attachment attachment) {
    return new AttachmentResponse(
        attachment.getId(),
        attachment.getEntityType(),
        attachment.getEntityId(),
        attachment.getFilename(),
        attachment.getContentType(),
        attachment.getSizeBytes(),
        attachment.getUploadedBy(),
        attachment.getCreatedAt());
  }
}
