package com.atomskills.askshoes.repository;

import com.atomskills.askshoes.entity.order.Order;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Spring Data репозиторий для {@link Order}. */
public interface OrderRepository
    extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

  /**
   * Страница заказов вместе с клиентом и статусом одним запросом — без догрузки на каждую строку.
   * Fetch-join в самой спецификации не подходит: он ломает count-запрос пагинации, а {@code
   * EntityGraph} к count не применяется.
   *
   * @param spec фильтр
   * @param pageable страница/размер/сортировка
   * @return страница заказов
   */
  @Override
  @EntityGraph(attributePaths = {"client", "status"})
  Page<Order> findAll(Specification<Order> spec, Pageable pageable);
}
