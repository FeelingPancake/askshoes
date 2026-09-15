package com.atomskills.argent.status;

import com.atomskills.argent.security.Role;
import com.atomskills.argent.security.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Единственная точка проверки "разрешён ли переход X→Y для данной статусной машины". Домен (заказы,
 * изделия и т.д.) вызывает {@link #assertTransitionAllowed} перед сменой статуса вместо того, чтобы
 * держать правила переходов в собственном коде.
 */
@Service
@RequiredArgsConstructor
public class StatusTransitionService {
  private final StatusTransitionRepository statusTransitionRepository;
  private final RoleRepository roleRepository;

  /**
   * Проверяет, что переход {@code fromStatusId → toStatusId} разрешён для статусной машины {@code
   * statusTypeId}, и что у текущего пользователя (из {@link SecurityContextHolder}) есть роль,
   * требуемая для этого перехода (если она задана). Ничего не делает при успешной проверке — сам
   * переход статуса выполняет вызывающий код.
   *
   * @param statusTypeId какая статусная машина ({@link StatusType#id})
   * @param fromStatusId текущий статус; {@code null} — начальный переход (создание сущности)
   * @param toStatusId статус, в который пытаются перейти
   * @throws EntityNotFoundException если такого перехода нет в {@code krn_status_transition}
   * @throws AccessDeniedException если переход требует роль, которой нет у текущего пользователя
   */
  public void assertTransitionAllowed(UUID statusTypeId, UUID fromStatusId, UUID toStatusId) {
    Specification<StatusTransition> spec =
        (root, query, cb) -> {
          Predicate statusTypePredicate = cb.equal(root.get("statusTypeId"), statusTypeId);
          Predicate toStatusPredicate = cb.equal(root.get("toStatusId"), toStatusId);
          Predicate fromStatusPredicate =
              fromStatusId == null
                  ? cb.isNull(root.get("fromStatusId"))
                  : cb.equal(root.get("fromStatusId"), fromStatusId);

          return cb.and(statusTypePredicate, toStatusPredicate, fromStatusPredicate);
        };

    Optional<StatusTransition> statusTransition =
        statusTransitionRepository.findAll(spec).stream().findFirst();

    if (statusTransition.isEmpty()) {
      throw new EntityNotFoundException("Не найден переход для заданной сущности");
    }

    if (statusTransition.get().getRequiredRoleId() != null) {
      StatusTransition transition = statusTransition.get();
      Role requiredRole =
          roleRepository
              .findById(transition.getRequiredRoleId())
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "Роль неизвестна:" + transition.getRequiredRoleId()));

      String requiredAuthority = "ROLE_" + requiredRole.getName();

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      boolean hasRole =
          Optional.ofNullable(authentication)
              .map(Authentication::getAuthorities)
              .map(
                  authorities ->
                      authorities.stream()
                          .map(GrantedAuthority::getAuthority)
                          .anyMatch(authority -> Objects.equals(authority, requiredAuthority)))
              .orElseThrow(() -> new IllegalStateException("Не найдены роли пользователя"));

      if (!hasRole) {
        throw new AccessDeniedException("Отсутствует роль: " + requiredRole.getName());
      }
    }
  }
}
