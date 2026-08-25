package io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.controller;

import io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.dto.PaymentMethodRegisterRequest;
import io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.dto.PaymentMethodResponse;
import io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.service.PaymentMethodService;
import io.github.spartateam6.commercepaymentsystem.global.annotation.MemberId;
import io.github.spartateam6.commercepaymentsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> register(
            @MemberId Long memberId,
            @Valid @RequestBody PaymentMethodRegisterRequest request
    ){
        PaymentMethodResponse response = paymentMethodService.register(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }
}
