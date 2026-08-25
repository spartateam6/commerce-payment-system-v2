package io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.repository;

import io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.entity.PaymentMethod;
import org.hibernate.internal.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    Optional<PaymentMethod> findByMember_IdAndIssueId(Long memberId, String issueId);
}
