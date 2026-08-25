package io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.repository;

import io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    Optional<PaymentMethod> findByMember_IdAndIssueId(Long memberId, String issueId);
}