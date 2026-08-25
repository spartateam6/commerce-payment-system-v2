package io.github.spartateam6.commercepaymentsystem.domain.refund.dto;

import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.Refund;
import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.RefundGatewayStatus;
import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.RefundItem;
import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.RefundStatus;

import java.util.List;

public record RefundResponse(
        Long refundId,
        RefundType refundType,
        RefundStatus status,
        RefundGatewayStatus gatewayStatus,
        Integer totalRefundAmount,
        Integer pgRefundAmount,
        Integer pointRefundAmount,
        Integer earnedPointRevokeAmount,
        List<RefundItemResponse> items
) {
    public static RefundResponse from(Refund refund) {
        return new RefundResponse(
                refund.getId(),
                refund.getRefundType(),
                refund.getStatus(),
                refund.getGatewayStatus(),
                refund.getTotalRefundAmount(),
                refund.getPgRefundAmount(),
                refund.getPointRefundAmount(),
                refund.getEarnedPointRevokeAmount(),
                refund.getRefundItems().stream().map(RefundItemResponse::from).toList()
        );
    }

    public record RefundItemResponse(
            Long orderItemId,
            Integer quantity,
            Integer totalRefundAmount,
            Integer pointRefundAmount,
            Integer pgRefundAmount
    ) {
        static RefundItemResponse from(RefundItem item) {
            return new RefundItemResponse(
                    item.getOrderItem().getId(), item.getQuantity(), item.getTotalRefundAmount(),
                    item.getPointRefundAmount(), item.getPgRefundAmount()
            );
        }
    }
}
