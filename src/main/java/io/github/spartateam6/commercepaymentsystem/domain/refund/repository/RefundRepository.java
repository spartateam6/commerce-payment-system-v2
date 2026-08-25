package io.github.spartateam6.commercepaymentsystem.domain.refund.repository;

import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    boolean existsByRequestKey(String requestKey);

    @Query("SELECT COALESCE(SUM(r.pointRefundAmount), 0) FROM Refund r WHERE r.payment.id = :paymentId")
    Integer sumPointRefundAmount(@Param("paymentId") Long paymentId);

    @Query("SELECT COALESCE(SUM(r.pgRefundAmount), 0) FROM Refund r WHERE r.payment.id = :paymentId")
    Integer sumPgRefundAmount(@Param("paymentId") Long paymentId);

    @Query("SELECT COALESCE(SUM(r.earnedPointRevokeAmount), 0) FROM Refund r WHERE r.payment.id = :paymentId")
    Integer sumEarnedPointRevokeAmount(@Param("paymentId") Long paymentId);
}
