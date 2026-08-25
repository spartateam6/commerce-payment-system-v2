package io.github.spartateam6.commercepaymentsystem.domain.refund.entity;

import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundType;
import io.github.spartateam6.commercepaymentsystem.dummy.PaymentFixture;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RefundTest {

    @Test
    void PG환불이_필요한_환불을_생성한다() {
        Payment payment = PaymentFixture.createPayment();
        Refund refund = Refund.complete(payment, "request-1", "단순 변심",
                10_000, 20_000, 200, RefundType.PARTIAL);

        assertEquals(30_000, refund.getTotalRefundAmount());
        assertEquals(RefundStatus.COMPLETED, refund.getStatus());
        assertEquals(RefundGatewayStatus.PENDING, refund.getGatewayStatus());
    }

    @Test
    void PG금액이_0이면_PG호출이_필요하지_않다() {
        Refund refund = Refund.complete(PaymentFixture.createPayment(),
                "request-1", "단순 변심", 30_000, 0, 0, RefundType.FULL);
        assertEquals(RefundGatewayStatus.NOT_REQUIRED, refund.getGatewayStatus());
    }

    @Test
    void PG실패와_후속성공을_각각_기록할_수_있다() {
        Refund refund = Refund.complete(PaymentFixture.createPayment(),
                "request-1", "단순 변심", 0, 30_000, 300, RefundType.FULL);
        refund.failGateway();
        assertEquals(RefundStatus.FAILED, refund.getStatus());
        assertEquals(RefundGatewayStatus.FAILED, refund.getGatewayStatus());

        refund.succeedGateway();
        assertEquals(RefundStatus.COMPLETED, refund.getStatus());
        assertEquals(RefundGatewayStatus.SUCCEEDED, refund.getGatewayStatus());
    }

    @Test
    void 요청키나_사유가_비어있으면_생성할_수_없다() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> Refund.complete(PaymentFixture.createPayment(),
                        " ", "단순 변심", 0, 30_000, 300, RefundType.FULL));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }
}
