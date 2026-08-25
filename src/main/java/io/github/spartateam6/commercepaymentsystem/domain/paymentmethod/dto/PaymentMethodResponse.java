package io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.dto;

import io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.entity.PaymentMethod;

public record PaymentMethodResponse(
        Long paymentMethodId,
        String cardCompany
) {
    public static PaymentMethodResponse from(PaymentMethod paymentMethod) {
        return new PaymentMethodResponse(
                paymentMethod.getId(),
                paymentMethod.getCardCompany()
        );
    }
}