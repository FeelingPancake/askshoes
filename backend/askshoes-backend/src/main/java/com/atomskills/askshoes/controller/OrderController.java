package com.atomskills.askshoes.controller;

import com.atomskills.askshoes.dto.order.OrderListItem;
import com.atomskills.askshoes.dto.order.OrderRequest;
import com.atomskills.askshoes.dto.order.OrderResponse;
import com.atomskills.askshoes.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** REST приёмки заказа: заказ сохраняется целиком одним запросом. */
@RestController
@RequestMapping("api/orders")
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orderService;

  /**
   * Создаёт заказ.
   *
   * @param request форма приёмки
   * @param authentication текущий пользователь — его {@code name} это {@code sub} JWT (id
   *     пользователя), он становится менеджером заказа
   * @return созданный заказ, {@code 201}
   * @throws com.atomskills.argent.error.ValidationErrorsException 400 при ошибках проверок
   */
  @Operation(operationId = "createOrder")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrderResponse create(
      @Valid @RequestBody OrderRequest request, Authentication authentication) {
    return orderService.create(request, UUID.fromString(authentication.getName()));
  }

  /**
   * Сохраняет заказ целиком.
   *
   * @param id id заказа
   * @param request форма приёмки
   * @return сохранённый заказ
   * @throws jakarta.persistence.EntityNotFoundException 404, если заказа нет
   * @throws com.atomskills.argent.error.ValidationErrorsException 400 при ошибках проверок
   */
  @Operation(operationId = "updateOrder")
  @PutMapping("{id}")
  public OrderResponse update(@PathVariable UUID id, @Valid @RequestBody OrderRequest request) {
    return orderService.update(id, request);
  }

  /**
   * @param id id заказа
   * @return заказ целиком
   * @throws jakarta.persistence.EntityNotFoundException 404, если заказа нет
   */
  @Operation(operationId = "getOrder")
  @GetMapping("{id}")
  public OrderResponse get(@PathVariable UUID id) {
    return orderService.get(id);
  }

  /**
   * Список заказов, по умолчанию — новые сверху.
   *
   * @param search подстрока номера/ФИО или цифры телефона
   * @param pageable страница/размер/сортировка
   * @return страница строк списка
   */
  @Operation(operationId = "listOrders")
  @GetMapping
  public Page<OrderListItem> list(
      @RequestParam(required = false) String search,
      @PageableDefault(sort = "acceptedAt", direction = Sort.Direction.DESC) Pageable pageable) {
    return orderService.list(search, pageable);
  }
}
