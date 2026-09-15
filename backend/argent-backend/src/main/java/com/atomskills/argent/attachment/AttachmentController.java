package com.atomskills.argent.attachment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST для {@code argent.attachment} — вложения произвольной сущности любого домена (see {@link
 * com.atomskills.argent.attachment package-info}). Регистрируется как {@code @Bean} в {@link
 * com.atomskills.argent.ArgentAutoConfiguration}
 *
 * <p><b>Известное ограничение (та же природа, что и упрощение в {@code ReferenceController}):</b>
 * ни один метод здесь не проверяет, что текущий пользователь имеет право видеть/менять именно
 * сущность {@code entityType}/{@code entityId}, к которой привязано вложение — только то, что
 * пользователь вообще аутентифицирован. Полноценная проверка ("может ли этот пользователь видеть
 * заказ №123") — доменная задача, ядро о ней ничего не знает; её решает authorization-слой домена,
 * который вызывает эти методы. Аналогично — {@code Content-Type} загруженного файла сейчас целиком
 * доверяется тому, что прислал клиент при загрузке, без allowlist разрешённых MIME-типов; риск
 * инлайн-рендеринга вредоносного содержимого браузером частично снижен тем, что {@link #download}
 * всегда шлёт {@code Content-Disposition: attachment} (скачивание, не показ в браузере) и заголовок
 * {@code X-Content-Type-Options: nosniff}, но это не замена настоящему allowlist'у.
 */
@RestController
@RequiredArgsConstructor
public class AttachmentController {
  private final AttachmentStorageService attachmentStorageService;
  private final AttachmentRepository attachmentRepository;

  /**
   * Загружает файл и привязывает его к сущности. {@code uploadedBy} не параметр запроса — берётся
   * из JWT текущего пользователя (тот же приём, что в {@code StatusTransitionService}).
   *
   * @param file содержимое файла (multipart/form-data)
   * @param entityType к какой сущности домена привязан файл (например {@code "Order"})
   * @param entityId id этой сущности
   * @return метаданные сохранённого вложения
   * @throws IOException если запись на диск не удалась
   */
  @PostMapping("api/attachments")
  public AttachmentResponse upload(
      @RequestParam MultipartFile file,
      @RequestParam String entityType,
      @RequestParam String entityId)
      throws IOException {
    UUID uploadedBy =
        UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    Attachment attachment = attachmentStorageService.store(file, entityType, entityId, uploadedBy);
    return AttachmentResponse.from(attachment);
  }

  /**
   * Отдаёт файл на скачивание с корректными заголовками ({@code Content-Type} из сохранённого
   * {@code contentType}, {@code Content-Disposition} с оригинальным именем файла, не {@code
   * storageKey}).
   *
   * @param id id вложения
   * @throws jakarta.persistence.EntityNotFoundException если такого вложения нет
   */
  @GetMapping("api/attachments/{id}")
  public ResponseEntity<Resource> download(@PathVariable UUID id) {
    Attachment attachment = attachmentStorageService.findOrThrow(id);
    Resource resource = attachmentStorageService.load(id);

    ContentDisposition contentDisposition =
        ContentDisposition.attachment()
            .filename(attachment.getFilename(), StandardCharsets.UTF_8)
            .build();

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(attachment.getContentType()))
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        .header("X-Content-Type-Options", "nosniff")
        .body(resource);
  }

  /** Список вложений конкретной сущности. */
  @GetMapping("api/attachments")
  public List<AttachmentResponse> list(
      @RequestParam String entityType, @RequestParam String entityId) {
    return attachmentRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
        .map(AttachmentResponse::from)
        .toList();
  }

  /**
   * Удаляет вложение — и файл на диске, и запись в БД (см. {@link AttachmentStorageService#delete}
   * про порядок и почему он именно такой).
   *
   * @throws IOException если удаление файла с диска не удалось
   */
  @DeleteMapping("api/attachments/{id}")
  public void delete(@PathVariable UUID id) throws IOException {
    attachmentStorageService.delete(id);
  }
}
