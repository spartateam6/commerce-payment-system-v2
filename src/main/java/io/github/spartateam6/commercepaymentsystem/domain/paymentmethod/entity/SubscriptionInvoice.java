package io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.entity;

import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.entity.AuditingEntity;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table (
    name = "subscription_invoice",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_subscription_invoice_period",
        columnNames = {"subscription_id", "billing_period_start"}
    )
)
public class SubscriptionInvoice extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "billing_period_start", nullable = false)
    private LocalDate billingPeriodStart;

    @Column(name = "billing_period_end", nullable = false)
    private LocalDate billingPeriodEnd;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionInvoiceStatus status;

    @Column(name = "portone_payment_id", nullable = false, unique = true, length = 100)
    private String portonePaymentId;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "fail_reason", length = 255)
    private String failReason;

    @Builder
    public SubscriptionInvoice(Subscription subscription, LocalDate billingPeriodStart, LocalDate billingPeriodEnd, Integer amount, String portonePaymentId) {
        this.subscription = subscription;
        this.billingPeriodStart = billingPeriodStart;
        this.billingPeriodEnd = billingPeriodEnd;
        this.amount = amount;
        this.status = SubscriptionInvoiceStatus.PENDING;
        this.portonePaymentId = portonePaymentId;
    }

    public void markSucceeded() {
        if (this.status.canTransitTo(SubscriptionInvoiceStatus.SUCCEEDED)) {
            throw new BusinessException(ErrorCode.INVALID_SUBSCRIPTION_STATUS);
        }
        this.status = SubscriptionInvoiceStatus.SUCCEEDED;
        this.paidAt = LocalDateTime.now();
    }
    public void markFailed(String reason) {
        if(!this.status.canTransitTo(SubscriptionInvoiceStatus.FAILED)) {
            throw new BusinessException(ErrorCode.INVALID_SUBSCRIPTION_STATUS);
        }
        this.status = SubscriptionInvoiceStatus.FAILED;
        this.failReason = reason;
    }

}
