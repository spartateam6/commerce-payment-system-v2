package io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.entity;

import io.github.spartateam6.commercepaymentsystem.global.entity.AuditingEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table (name = "payment_method", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_method_billing_key", columnNames = "billing_key"),
        @UniqueConstraint(name = "uk_payment_method_member_issue", columnNames = {"member_id", "issue_id"})
}
)
public class PaymentMethod extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "billing_key", nullable = false, length = 200)
    private String billingKey;

    @Column(name = "issue_id", nullable = false, length = 100)
    private String issueId; // 클라이언트 발급 요청 ID -> 멱등 키로 사용

    @Column(name = "card_company", nullable = false, length = 50)
    private String cardCompany;

    @Builder
    public PaymentMethod(Member member, String billingKey, String issueId, String cardCompany) {
        this.member = member;
        this.billingKey = billingKey;
        this.issueId = issueId;
        this.cardCompany = cardCompany;
    }
}
