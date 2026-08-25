package io.github.spartateam6.commercepaymentsystem.domain.refund.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RefundRequest(
        @NotNull @Positive Long paymentId,
        @NotBlank @Size(max = 100) String requestKey,
        @NotBlank @Size(max = 500) String cancelReason,
        @NotEmpty List<@Valid RefundItemRequest> items
) {
    public record RefundItemRequest(
            @NotNull @Positive Long orderItemId,
            @NotNull @Positive Integer quantity
    ) {
    }
}
