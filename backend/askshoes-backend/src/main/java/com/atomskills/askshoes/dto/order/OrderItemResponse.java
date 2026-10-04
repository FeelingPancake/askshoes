package com.atomskills.askshoes.dto.order;

import com.atomskills.askshoes.entity.order.OrderItem;
import com.atomskills.askshoes.entity.order.OrderItemDefect;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Изделие в ответе заказа: поля запроса плюс вычисленные.
 *
 * @param id id изделия
 * @param number номер изделия {@code <номер заказа>-<seqNo>/<кол-во изделий>}
 * @param seqNo порядковый номер в заказе
 * @param categoryId позиция {@code ITEM_CATEGORY}
 * @param itemTypeId позиция {@code ITEM_TYPE}
 * @param brandId позиция {@code BRAND}
 * @param warranty гарантийный случай
 * @param colorId позиция {@code COLOR}
 * @param materialId позиция {@code MATERIAL}
 * @param sizeId позиция {@code ITEM_SIZE}
 * @param wearPercent износ, 0–100
 * @param comment комментарий
 * @param defectTypeIds позиции {@code DEFECT_TYPE}
 * @param works работы
 * @param worksTotal сумма работ изделия
 */
public record OrderItemResponse(
    UUID id,
    String number,
    Integer seqNo,
    UUID categoryId,
    UUID itemTypeId,
    UUID brandId,
    Boolean warranty,
    UUID colorId,
    UUID materialId,
    UUID sizeId,
    Integer wearPercent,
    String comment,
    List<UUID> defectTypeIds,
    List<OrderItemWorkResponse> works,
    BigDecimal worksTotal) {

  /**
   * @param item изделие
   * @param orderNumber номер заказа
   * @param itemsCount количество изделий в заказе
   * @return DTO изделия с вычисленным номером
   */
  public static OrderItemResponse from(OrderItem item, String orderNumber, int itemsCount) {
    return new OrderItemResponse(
        item.getId(),
        "%s-%d/%d".formatted(orderNumber, item.getSeqNo(), itemsCount),
        item.getSeqNo(),
        item.getCategoryId(),
        item.getItemTypeId(),
        item.getBrandId(),
        item.getWarranty(),
        item.getColorId(),
        item.getMaterialId(),
        item.getSizeId(),
        item.getWearPercent(),
        item.getComment(),
        item.getDefects().stream().map(OrderItemDefect::getDefectTypeId).toList(),
        item.getWorks().stream().map(OrderItemWorkResponse::from).toList(),
        item.getWorksTotal());
  }
}
