package io.github.spartateam6.commercepaymentsystem.domain.order.service;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderItem;
import io.github.spartateam6.commercepaymentsystem.domain.order.repository.OrderItemRepository;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import io.github.spartateam6.commercepaymentsystem.domain.product.repository.ProductRepository;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void restoreOrderProductStock(Long orderId) {
        List<OrderItem> orderItemList = orderItemRepository.findByOrder_Id(orderId);

        for (OrderItem orderItem : orderItemList) {
            Product product = orderItem.getProduct();
            Integer stock = orderItem.getQuantity();

            product.addStock(stock);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void restoreRefundedStock(List<RestoreStock> restoreStocks) {
        Set<Long> productIds = restoreStocks.stream()
                .map(item -> item.orderItem().getProduct().getId())
                .collect(Collectors.toSet());
        Map<Long, Product> lockedProducts = productRepository.findAllByIdInForUpdate(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        if (lockedProducts.size() != productIds.size()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        restoreStocks.stream()
                .sorted((left, right) -> Long.compare(
                        left.orderItem().getProduct().getId(), right.orderItem().getProduct().getId()))
                .forEach(item -> lockedProducts.get(item.orderItem().getProduct().getId()).addStock(item.quantity()));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    public List<Long> getCartItemIds(Long orderId) {
        return orderItemRepository.findCartItemIdsByOrderId(orderId);
    }

    public record RestoreStock(OrderItem orderItem, int quantity) {
    }

}
