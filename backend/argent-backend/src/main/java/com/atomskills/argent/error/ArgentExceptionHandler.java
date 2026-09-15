package com.atomskills.argent.error;

import jakarta.persistence.EntityNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Единый формат ошибок (RFC 7807 {@link ProblemDetail}) для любого контроллера в любом модуле,
 * подключившем {@code argent-backend} — независимо от того, в каком jar лежит контроллер.
 */
@Slf4j
@RestControllerAdvice
public class ArgentExceptionHandler extends ResponseEntityExceptionHandler {
  @ExceptionHandler(EntityNotFoundException.class)
  public ProblemDetail handleNotFound(EntityNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleBadRequest(IllegalArgumentException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ProblemDetail handleConflict(IllegalStateException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ProblemDetail handleDataIntegrity(DataIntegrityViolationException e) {
    log.warn("Data integrity violation", e);
    return problem(
        HttpStatus.CONFLICT, "Запись нарушает ограничение БД (дубликат или есть ссылки)");
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleForbidden(AccessDeniedException e) {
    return problem(HttpStatus.FORBIDDEN, e.getMessage());
  }

  @ExceptionHandler(AuthenticationException.class)
  public ProblemDetail handleUnauthorized(AuthenticationException e) {
    return problem(HttpStatus.UNAUTHORIZED, e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleException(Exception e) {
    log.error("Unhandled ex", e);
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    Map<String, String> errors = new LinkedHashMap<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
    }

    ProblemDetail body = ex.getBody();
    body.setDetail("Ошибка валидации");
    body.setProperty("errors", errors);
    return handleExceptionInternal(ex, body, headers, status, request);
  }

  private ProblemDetail problem(HttpStatus status, String detail) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
    problemDetail.setTitle(status.getReasonPhrase());
    return problemDetail;
  }
}
