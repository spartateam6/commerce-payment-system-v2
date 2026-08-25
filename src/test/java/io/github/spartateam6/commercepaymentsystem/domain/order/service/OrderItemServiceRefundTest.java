package io.github.spartateam6.commercepaymentsystem.domain.order.service;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderItem;
import io.github.spartateam6.commercepaymentsystem.domain.order.repository.OrderItemRepository;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.SaleStatus;
import io.github.spartateam6.commercepaymentsystem.domain.product.repository.ProductRepository;
import io.github.spartateam6.commercepaymentsystem.dummy.MemberFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceRefundTest {
    @InjectMocks OrderItemService orderItemService;
    @Mock OrderItemRepository orderItemRepository;
    @Mock ProductRepository productRepository;

    @Test
    void 환불대상_수량만큼만_잠긴_상품재고를_복구한다() {
        Product product = Product.builder().id(1L).name("상품").price(10_000).stock(5)
                .saleStatus(SaleStatus.ON_SALE).description("설명").category("기타").build();
        Order order = Order.create(MemberFixture.members.get(0), "ORD-1", 0);
        order.addOrderItem(1L, product, "상품", 10_000, 3);
        OrderItem orderItem = order.getOrderItems().get(0);
        given(productRepository.findAllByIdInForUpdate(java.util.Set.of(1L)))
                .willReturn(List.of(product));

        orderItemService.restoreRefundedStock(
                List.of(new OrderItemService.RestoreStock(orderItem, 2)));

        assertEquals(7, product.getStock());
    }
}
