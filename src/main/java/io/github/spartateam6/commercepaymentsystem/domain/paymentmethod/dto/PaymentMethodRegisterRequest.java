package io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentMethodRegisterRequest (
        @NotBlank String billingkey,
        @NotBlank String issueId,
        @NotBlank String cardCompany
){}

