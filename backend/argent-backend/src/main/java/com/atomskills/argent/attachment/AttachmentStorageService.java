package com.atomskills.argent.attachment;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AttachmentStorageService {
  @Value("${argent.storage.root-path}")
  private final String storagePath;

  private final AttachmentRepository attachmentRepository;

  public Attachment store(MultipartFile file, String entityType, String entityId, UUID uploadedBy)
      throws IOException {
    String storageKey = UUID.randomUUID().toString();
    Path targetPath = Paths.get(storagePath, storageKey);
    Files.createDirectories(Paths.get(storagePath));

    file.transferTo(targetPath);
    Attachment attachment = new Attachment();
    attachment.setEntityId(entityId);
    attachment.setEntityType(entityType);
    attachment.setFilename(file.getOriginalFilename());
    attachment.setSizeBytes(file.getSize());
    attachment.setStorageKey(storageKey);
    attachment.setContentType(file.getContentType());
    attachment.setUploadedBy(uploadedBy);

    return attachmentRepository.save(attachment);
  }

  public Resource load(UUID attachmentId) {
    Attachment attachment = findOrThrow(attachmentId);
    return new FileSystemResource(Paths.get(storagePath, attachment.getStorageKey()));
  }

  public void delete(UUID attachmentId) throws IOException {
    Attachment attachment = findOrThrow(attachmentId);
    Files.delete(Paths.get(storagePath, attachment.getStorageKey()));
    attachmentRepository.deleteById(attachmentId);
  }

  Attachment findOrThrow(UUID attachmentId) {
    return attachmentRepository
        .findById(attachmentId)
        .orElseThrow(() -> new EntityNotFoundException("Не найден файл с " + attachmentId));
  }
}
