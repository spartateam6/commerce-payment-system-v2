package io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.entity;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.entity.AuditingEntity;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.coyote.BadRequestException;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "subscription")
public class Subscription extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionSatatus status;

    @Column(name = "next_billing_date", nullable = false)
    private LocalDate nextBillingDate;

    @Column(name = "cancelled_at")
    private LocalDate cancelledAt;

    @Builder
    public Subscription(Member member, PaymentMethod paymentMethod, String planName, Integer amount, LocalDate nextBillingDate, LocalDate cancelledAt) {
        this.member = member;
        this.paymentMethod = paymentMethod;
        this.planName = planName;
        this.amount = amount;
        this.status = SubscriptionSatatus.ACTIVE;
        this.nextBillingDate = nextBillingDate;
    }

    public void advanceNextBillingDate(LocalDate newDate) {
        this.nextBillingDate = newDate;
    }

    public void changeStatus(SubscriptionSatatus newStatus) {
        if(!this.status.canTransitTo(newStatus)) {
            throw new BusinessException(ErrorCode.INVALID_SUBSCRIPTION_STATUS);
        }
        this.status = newStatus;
        if (newStatus == SubscriptionSatatus.CANCELLED) {
            this.cancelledAt = LocalDate.now();
        }
    }

    public boolean isOwnedBy(Long memberId) {
        return this.member.getId().equals(memberId);
    }
}
