package io.github.spartateam6.commercepaymentsystem.domain.refund.controller;

import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundRequest;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundResponse;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundType;
import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.RefundGatewayStatus;
import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.RefundStatus;
import io.github.spartateam6.commercepaymentsystem.domain.refund.facade.RefundFacade;
import io.github.spartateam6.commercepaymentsystem.global.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RefundControllerTest {
    @InjectMocks RefundController refundController;
    @Mock RefundFacade refundFacade;

    @Test
    void refund_성공하면_환불결과와_200을_반환한다() {
        RefundRequest request = new RefundRequest(10L, "request-1", "단순 변심",
                List.of(new RefundRequest.RefundItemRequest(20L, 1)));
        RefundResponse result = new RefundResponse(100L, RefundType.PARTIAL,
                RefundStatus.COMPLETED, RefundGatewayStatus.SUCCEEDED,
                15_000, 10_000, 5_000, 100, List.of());
        given(refundFacade.refund(1L, request)).willReturn(result);

        ResponseEntity<ApiResponse<RefundResponse>> response = refundController.refund(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEqualTo(result);
    }
}
