package io.github.spartateam6.commercepaymentsystem.domain.refund.facade;

import io.github.spartateam6.commercepaymentsystem.domain.payment.port.PaymentGateway;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundRequest;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundResponse;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundType;
import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.RefundGatewayStatus;
import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.RefundStatus;
import io.github.spartateam6.commercepaymentsystem.domain.refund.service.RefundService;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class RefundFacadeTest {
    @InjectMocks RefundFacade refundFacade;
    @Mock RefundService refundService;
    @Mock PaymentGateway paymentGateway;
    RefundRequest request;
    RefundResponse pending;

    @BeforeEach
    void setUp() {
        request = new RefundRequest(10L, "request-1", "단순 변심",
                List.of(new RefundRequest.RefundItemRequest(20L, 1)));
        pending = new RefundResponse(100L, RefundType.PARTIAL, RefundStatus.COMPLETED,
                RefundGatewayStatus.PENDING, 15_000, 10_000, 5_000, 100, List.of());
    }

    @Test
    void pg금액이_있으면_선검증과_DB반영_후_PG를_취소한다() {
        RefundResponse succeeded = new RefundResponse(100L, RefundType.PARTIAL,
                RefundStatus.COMPLETED, RefundGatewayStatus.SUCCEEDED,
                15_000, 10_000, 5_000, 100, List.of());
        given(refundService.process(1L, request)).willReturn(
                new RefundService.RefundResult(pending, "payment-1", 10_000, "단순 변심"));
        given(refundService.markGatewaySucceeded(100L)).willReturn(succeeded);

        assertEquals(succeeded, refundFacade.refund(1L, request));
        then(refundService).should().validate(1L, request);
        then(refundService).should().markGatewayRequested(100L);
        then(paymentGateway).should().cancelPayment("payment-1", "단순 변심", 10_000L);
    }

    @Test
    void 포인트전액결제는_PG를_호출하지_않는다() {
        RefundResponse response = new RefundResponse(100L, RefundType.FULL,
                RefundStatus.COMPLETED, RefundGatewayStatus.NOT_REQUIRED,
                30_000, 0, 30_000, 0, List.of());
        given(refundService.process(1L, request)).willReturn(
                new RefundService.RefundResult(response, "payment-1", 0, "단순 변심"));

        assertEquals(response, refundFacade.refund(1L, request));
        then(paymentGateway).shouldHaveNoInteractions();
    }

    @Test
    void PG취소가_실패하면_실패상태로_변경한다() {
        given(refundService.process(1L, request)).willReturn(
                new RefundService.RefundResult(pending, "payment-1", 10_000, "단순 변심"));
        willThrow(new BusinessException(ErrorCode.PAYMENT_GATEWAY_ERROR)).given(paymentGateway)
                .cancelPayment("payment-1", "단순 변심", 10_000L);

        assertThrows(BusinessException.class, () -> refundFacade.refund(1L, request));
        then(refundService).should().markFailed(100L);
    }
}
