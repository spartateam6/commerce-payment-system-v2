package io.github.spartateam6.commercepaymentsystem.domain.refund.service;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderItem;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderStatus;
import io.github.spartateam6.commercepaymentsystem.domain.order.repository.OrderItemRepository;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderItemService;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.PaymentStatus;
import io.github.spartateam6.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import io.github.spartateam6.commercepaymentsystem.domain.point.service.PointService;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundRequest;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundResponse;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundType;
import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.Refund;
import io.github.spartateam6.commercepaymentsystem.domain.refund.repository.RefundRepository;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final OrderItemRepository orderItemRepository;
    private final PointService pointService;
    private final OrderItemService orderItemService;

    public void validate(Long memberId, RefundRequest request) {
        Payment payment = paymentRepository.findByIdWithOrderAndMember(request.paymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        validatePayment(memberId, payment);
        validateRequestKey(request.requestKey());
        validateItems(payment.getOrder(), request);
    }

    @Transactional
    public RefundResult process(Long memberId, RefundRequest request) {
        Payment payment = paymentRepository.findByIdWithOrderAndMemberLock(request.paymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        validatePayment(memberId, payment);
        validateRequestKey(request.requestKey());

        Order order = payment.getOrder();
        List<OrderItem> orderItems = orderItemRepository.findByOrder_Id(order.getId());
        Map<Long, Integer> requestedQuantities = requestedQuantities(request);
        Map<Long, OrderItem> itemsById = new HashMap<>();
        orderItems.forEach(item -> itemsById.put(item.getId(), item));
        validateRequestedItems(itemsById, requestedQuantities);

        boolean fullRefund = orderItems.stream().allMatch(item ->
                item.getRefundableQuantity() - requestedQuantities.getOrDefault(item.getId(), 0) == 0);
        int totalRefundAmount = requestedQuantities.entrySet().stream()
                .mapToInt(entry -> itemsById.get(entry.getKey()).getUnitPriceSnapshot() * entry.getValue())
                .sum();

        RefundAmounts amounts = calculateAmounts(payment, totalRefundAmount, fullRefund);
        Refund refund = Refund.complete(payment, request.requestKey(), request.cancelReason(),
                amounts.point(), amounts.pg(), amounts.earnedRevoke(),
                fullRefund ? RefundType.FULL : RefundType.PARTIAL);

        List<ItemAllocation> allocations = allocateItems(itemsById, requestedQuantities, amounts);
        allocations.forEach(allocation -> refund.addItem(
                allocation.item(), allocation.quantity(), allocation.point(), allocation.pg()));
        refundRepository.save(refund);

        pointService.applyRefundPoint(memberId, payment, refund,
                amounts.point(), amounts.earnedRevoke());
        allocations.forEach(allocation -> allocation.item().refundQuantity(allocation.quantity()));
        orderItemService.restoreRefundedStock(allocations.stream()
                .map(allocation -> new OrderItemService.RestoreStock(
                        allocation.item(), allocation.quantity()))
                .toList());

        payment.applyRefund(fullRefund);
        if (fullRefund) {
            order.updateStatus(OrderStatus.CANCELLED);
        }

        return new RefundResult(RefundResponse.from(refund),
                payment.getPortonePaymentId(), amounts.pg(), request.cancelReason());
    }

    @Transactional
    public RefundResponse markGatewaySucceeded(Long refundId) {
        Refund refund = findRefund(refundId);
        refund.succeedGateway();
        return RefundResponse.from(refund);
    }

    @Transactional
    public void markGatewayRequested(Long refundId) {
        findRefund(refundId).markPgRequested();
    }

    @Transactional
    public void markFailed(Long refundId) {
        findRefund(refundId).failGateway();
    }

    private Refund findRefund(Long refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFUND_NOT_FOUND));
    }

    private void validatePayment(Long memberId, Payment payment) {
        Order order = payment.getOrder();
        if (!order.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        if ((payment.getStatus() != PaymentStatus.PAID
                && payment.getStatus() != PaymentStatus.PARTIAL_REFUND)
                || order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_STATUS);
        }
        if (payment.getOrderAmount() == null || payment.getOrderAmount() < 0
                || payment.getPointUsedAmount() == null || payment.getPointUsedAmount() < 0
                || payment.getPgAmount() == null || payment.getPgAmount() < 0
                || payment.getEarnedPointAmount() == null || payment.getEarnedPointAmount() < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "환불 기준 금액이 올바르지 않습니다.");
        }
        if (payment.getPgAmount() > 0
                && (payment.getPortonePaymentId() == null || payment.getPortonePaymentId().isBlank())) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_MATCH_ORDER);
        }
    }

    private void validateRequestKey(String requestKey) {
        if (refundRepository.existsByRequestKey(requestKey)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REFUND_REQUEST);
        }
    }

    private void validateItems(Order order, RefundRequest request) {
        Map<Long, OrderItem> itemsById = new HashMap<>();
        orderItemRepository.findByOrder_Id(order.getId())
                .forEach(item -> itemsById.put(item.getId(), item));
        validateRequestedItems(itemsById, requestedQuantities(request));
    }

    private Map<Long, Integer> requestedQuantities(RefundRequest request) {
        Map<Long, Integer> quantities = new HashMap<>();
        for (RefundRequest.RefundItemRequest item : request.items()) {
            if (quantities.putIfAbsent(item.orderItemId(), item.quantity()) != null) {
                throw new BusinessException(ErrorCode.INVALID_REFUND_ITEM, "환불 대상 주문 상품이 중복되었습니다.");
            }
        }
        return quantities;
    }

    private void validateRequestedItems(Map<Long, OrderItem> itemsById, Map<Long, Integer> quantities) {
        for (Map.Entry<Long, Integer> request : quantities.entrySet()) {
            OrderItem item = itemsById.get(request.getKey());
            if (item == null) {
                throw new BusinessException(ErrorCode.INVALID_REFUND_ITEM);
            }
            if (request.getValue() > item.getRefundableQuantity()) {
                throw new BusinessException(ErrorCode.REFUND_QUANTITY_EXCEEDED);
            }
        }
    }

    private RefundAmounts calculateAmounts(Payment payment, int total, boolean fullRefund) {
        if (fullRefund) {
            return new RefundAmounts(
                    payment.getPointUsedAmount() - refundRepository.sumPointRefundAmount(payment.getId()),
                    payment.getPgAmount() - refundRepository.sumPgRefundAmount(payment.getId()),
                    payment.getEarnedPointAmount() - refundRepository.sumEarnedPointRevokeAmount(payment.getId())
            );
        }
        if (payment.getOrderAmount() == 0) {
            return new RefundAmounts(0, 0, 0);
        }
        int point = (int) ((long) total * payment.getPointUsedAmount() / payment.getOrderAmount());
        int pg = total - point;
        int earnedRevoke = payment.getPgAmount() == 0 ? 0
                : (int) ((long) payment.getEarnedPointAmount() * pg / payment.getPgAmount());
        return new RefundAmounts(point, pg, earnedRevoke);
    }

    private List<ItemAllocation> allocateItems(Map<Long, OrderItem> itemsById,
                                               Map<Long, Integer> quantities,
                                               RefundAmounts totals) {
        List<Map.Entry<Long, Integer>> entries = quantities.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();
        int totalAmount = entries.stream()
                .mapToInt(entry -> itemsById.get(entry.getKey()).getUnitPriceSnapshot() * entry.getValue())
                .sum();
        int allocatedPoint = 0;
        int allocatedPg = 0;
        List<ItemAllocation> result = new ArrayList<>();
        for (int index = 0; index < entries.size(); index++) {
            Map.Entry<Long, Integer> entry = entries.get(index);
            OrderItem item = itemsById.get(entry.getKey());
            int lineAmount = item.getUnitPriceSnapshot() * entry.getValue();
            boolean last = index == entries.size() - 1;
            int point = totalAmount == 0 ? 0 : last ? totals.point() - allocatedPoint
                    : (int) ((long) totals.point() * lineAmount / totalAmount);
            int pg = totalAmount == 0 ? 0 : last ? totals.pg() - allocatedPg : lineAmount - point;
            result.add(new ItemAllocation(item, entry.getValue(), point, pg));
            allocatedPoint += point;
            allocatedPg += pg;
        }
        return result;
    }

    public record RefundResult(RefundResponse response, String portonePaymentId,
                               Integer pgRefundAmount, String cancelReason) {
    }

    private record RefundAmounts(int point, int pg, int earnedRevoke) {
        private RefundAmounts {
            if (point < 0 || pg < 0 || earnedRevoke < 0) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "누적 환불 금액이 결제 금액을 초과했습니다.");
            }
        }
    }

    private record ItemAllocation(OrderItem item, int quantity, int point, int pg) {
    }
}
