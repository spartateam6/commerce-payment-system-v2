package io.github.spartateam6.commercepaymentsystem.domain.refund.repository;

import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.RefundItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundItemRepository extends JpaRepository<RefundItem, Long> {
}
