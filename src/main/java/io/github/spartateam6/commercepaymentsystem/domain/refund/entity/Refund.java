package io.github.spartateam6.commercepaymentsystem.domain.refund.entity;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderItem;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundType;
import io.github.spartateam6.commercepaymentsystem.global.entity.AuditingEntity;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode.INVALID_INPUT;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "refund", uniqueConstraints = @UniqueConstraint(
        name = "uk_refund_request_key",
        columnNames = "request_key"
))
public class Refund extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "request_key", nullable = false, length = 100)
    private String requestKey;

    @Lob
    @Column(name = "cancel_reason", nullable = false)
    private String cancelReason;

    @Column(name = "total_refund_amount", nullable = false)
    private Integer totalRefundAmount;

    @Column(name = "point_refund_amount", nullable = false)
    private Integer pointRefundAmount;

    @Column(name = "pg_refund_amount", nullable = false)
    private Integer pgRefundAmount;

    @Column(name = "earned_point_revoke_amount", nullable = false)
    private Integer earnedPointRevokeAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type", nullable = false, length = 20)
    private RefundType refundType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RefundStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "gateway_status", nullable = false, length = 30)
    private RefundGatewayStatus gatewayStatus;

    @Column(name = "pg_cancel_requested_at")
    private LocalDateTime pgCancelRequestedAt;

    @Column(name = "pg_cancel_completed_at")
    private LocalDateTime pgCancelCompletedAt;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @OneToMany(mappedBy = "refund", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefundItem> refundItems = new ArrayList<>();

    public static Refund complete(Payment payment, String requestKey, String cancelReason,
                                  int pointRefundAmount, int pgRefundAmount,
                                  int earnedPointRevokeAmount, RefundType refundType) {
        if (payment == null) {
            throw new BusinessException(INVALID_INPUT, "결제 정보는 필수입니다.");
        }
        if (requestKey == null || requestKey.isBlank()) {
            throw new BusinessException(INVALID_INPUT, "환불 요청 키는 필수입니다.");
        }
        if (cancelReason == null || cancelReason.isBlank()) {
            throw new BusinessException(INVALID_INPUT, "취소 사유는 필수입니다.");
        }
        if (pointRefundAmount < 0 || pgRefundAmount < 0 || earnedPointRevokeAmount < 0) {
            throw new BusinessException(INVALID_INPUT, "환불 금액은 0 이상이어야 합니다.");
        }
        if (refundType == null) {
            throw new BusinessException(INVALID_INPUT, "환불 유형은 필수입니다.");
        }

        Refund refund = new Refund();
        refund.payment = payment;
        refund.requestKey = requestKey;
        refund.cancelReason = cancelReason;
        refund.pointRefundAmount = pointRefundAmount;
        refund.pgRefundAmount = pgRefundAmount;
        refund.totalRefundAmount = pointRefundAmount + pgRefundAmount;
        refund.earnedPointRevokeAmount = earnedPointRevokeAmount;
        refund.refundType = refundType;
        refund.status = RefundStatus.COMPLETED;
        refund.gatewayStatus = pgRefundAmount == 0
                ? RefundGatewayStatus.NOT_REQUIRED
                : RefundGatewayStatus.PENDING;
        refund.retryCount = 0;
        return refund;
    }

    public void addItem(OrderItem orderItem, int quantity, int pointAmount, int pgAmount) {
        refundItems.add(RefundItem.create(this, orderItem, quantity, pointAmount, pgAmount));
    }

    public List<RefundItem> getRefundItems() {
        return Collections.unmodifiableList(refundItems);
    }

    public void markPgRequested() {
        pgCancelRequestedAt = LocalDateTime.now();
    }

    public void succeedGateway() {
        if (gatewayStatus == RefundGatewayStatus.SUCCEEDED) {
            return;
        }
        gatewayStatus = RefundGatewayStatus.SUCCEEDED;
        status = RefundStatus.COMPLETED;
        pgCancelCompletedAt = LocalDateTime.now();
    }

    public void failGateway() {
        if (gatewayStatus == RefundGatewayStatus.SUCCEEDED) {
            return;
        }
        gatewayStatus = RefundGatewayStatus.FAILED;
        status = RefundStatus.FAILED;
    }
}
