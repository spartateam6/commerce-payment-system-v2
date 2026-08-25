package io.github.spartateam6.commercepaymentsystem.domain.refund.service;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderItem;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderStatus;
import io.github.spartateam6.commercepaymentsystem.domain.order.repository.OrderItemRepository;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderItemService;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.PaymentStatus;
import io.github.spartateam6.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import io.github.spartateam6.commercepaymentsystem.domain.point.service.PointService;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.SaleStatus;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundRequest;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundType;
import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.Refund;
import io.github.spartateam6.commercepaymentsystem.domain.refund.repository.RefundRepository;
import io.github.spartateam6.commercepaymentsystem.dummy.PaymentFixture;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {
    @InjectMocks RefundService refundService;
    @Mock PaymentRepository paymentRepository;
    @Mock RefundRepository refundRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock PointService pointService;
    @Mock OrderItemService orderItemService;

    Payment payment;
    OrderItem orderItem;

    @BeforeEach
    void setUp() {
        payment = PaymentFixture.createPayment();
        ReflectionTestUtils.setField(payment, "id", 10L);
        ReflectionTestUtils.setField(payment, "pointUsedAmount", 10_000);
        ReflectionTestUtils.setField(payment.getOrder(), "id", 20L);
        Product product = Product.builder().id(1L).name("상품").price(15_000).stock(10)
                .saleStatus(SaleStatus.ON_SALE).description("설명").category("기타").build();
        payment.getOrder().addOrderItem(1L, product, "상품", 15_000, 2);
        orderItem = payment.getOrder().getOrderItems().get(0);
        ReflectionTestUtils.setField(orderItem, "id", 30L);
        payment.changeStatus(PaymentStatus.PAID, 20_000);
        payment.getOrder().updateStatus(OrderStatus.CONFIRMED);

        given(paymentRepository.findByIdWithOrderAndMemberLock(10L)).willReturn(Optional.of(payment));
        lenient().when(orderItemRepository.findByOrder_Id(20L)).thenReturn(List.of(orderItem));
        lenient().when(refundRepository.save(any(Refund.class))).thenAnswer(invocation -> {
            Refund refund = invocation.getArgument(0);
            ReflectionTestUtils.setField(refund, "id", 100L);
            return refund;
        });
    }

    private RefundRequest request(int quantity, String key) {
        return new RefundRequest(10L, key, "단순 변심",
                List.of(new RefundRequest.RefundItemRequest(30L, quantity)));
    }

    @Test
    void 복합결제의_절반을_비율대로_부분환불한다() {
        RefundService.RefundResult result = refundService.process(1L, request(1, "request-1"));

        assertEquals(RefundType.PARTIAL, result.response().refundType());
        assertEquals(15_000, result.response().totalRefundAmount());
        assertEquals(5_000, result.response().pointRefundAmount());
        assertEquals(10_000, result.response().pgRefundAmount());
        assertEquals(100, result.response().earnedPointRevokeAmount());
        assertEquals(PaymentStatus.PARTIAL_REFUND, payment.getStatus());
        assertEquals(OrderStatus.CONFIRMED, payment.getOrder().getStatus());
        assertEquals(1, orderItem.getRefundedQuantity());
        then(pointService).should().applyRefundPoint(
                eq(1L), eq(payment), any(Refund.class), eq(5_000), eq(100));
    }

    @Test
    void 마지막_수량을_환불하면_잔여금액으로_전액보정한다() {
        ReflectionTestUtils.setField(orderItem, "refundedQuantity", 1);
        ReflectionTestUtils.setField(payment, "status", PaymentStatus.PARTIAL_REFUND);
        given(refundRepository.sumPointRefundAmount(10L)).willReturn(4_999);
        given(refundRepository.sumPgRefundAmount(10L)).willReturn(10_001);
        given(refundRepository.sumEarnedPointRevokeAmount(10L)).willReturn(99);

        RefundService.RefundResult result = refundService.process(1L, request(1, "request-2"));

        assertEquals(RefundType.FULL, result.response().refundType());
        assertEquals(5_001, result.response().pointRefundAmount());
        assertEquals(9_999, result.response().pgRefundAmount());
        assertEquals(101, result.response().earnedPointRevokeAmount());
        assertEquals(PaymentStatus.REFUND, payment.getStatus());
        assertEquals(OrderStatus.CANCELLED, payment.getOrder().getStatus());
    }

    @Test
    void 잔여수량을_초과하면_거부한다() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> refundService.process(1L, request(3, "request-1")));
        assertEquals(ErrorCode.REFUND_QUANTITY_EXCEEDED, exception.getErrorCode());
    }

    @Test
    void 같은_요청키는_거부한다() {
        given(refundRepository.existsByRequestKey("request-1")).willReturn(true);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> refundService.process(1L, request(1, "request-1")));
        assertEquals(ErrorCode.DUPLICATE_REFUND_REQUEST, exception.getErrorCode());
    }

    @Test
    void 다른_회원의_결제는_환불할_수_없다() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> refundService.process(2L, request(1, "request-1")));
        assertEquals(ErrorCode.FORBIDDEN_ACCESS, exception.getErrorCode());
    }
}
