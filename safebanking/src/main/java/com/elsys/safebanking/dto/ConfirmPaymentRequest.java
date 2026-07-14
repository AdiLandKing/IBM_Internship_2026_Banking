package com.elsys.safebanking.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmPaymentRequest(
        @NotBlank(message = "Payment Intent ID is required")
        String paymentIntentId
) {
}
