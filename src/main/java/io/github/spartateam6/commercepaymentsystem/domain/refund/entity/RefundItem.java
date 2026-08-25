package io.github.spartateam6.commercepaymentsystem.domain.refund.entity;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderItem;
import io.github.spartateam6.commercepaymentsystem.global.entity.AuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "refund_item", uniqueConstraints = @UniqueConstraint(
        name = "uk_refund_item_refund_order_item",
        columnNames = {"refund_id", "order_item_id"}
))
public class RefundItem extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "refund_id", nullable = false)
    private Refund refund;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price_snapshot", nullable = false)
    private Integer unitPriceSnapshot;

    @Column(name = "total_refund_amount", nullable = false)
    private Integer totalRefundAmount;

    @Column(name = "point_refund_amount", nullable = false)
    private Integer pointRefundAmount;

    @Column(name = "pg_refund_amount", nullable = false)
    private Integer pgRefundAmount;

    static RefundItem create(Refund refund, OrderItem orderItem, int quantity,
                             int pointRefundAmount, int pgRefundAmount) {
        RefundItem item = new RefundItem();
        item.refund = refund;
        item.orderItem = orderItem;
        item.quantity = quantity;
        item.unitPriceSnapshot = orderItem.getUnitPriceSnapshot();
        item.totalRefundAmount = item.unitPriceSnapshot * quantity;
        item.pointRefundAmount = pointRefundAmount;
        item.pgRefundAmount = pgRefundAmount;
        return item;
    }
}
