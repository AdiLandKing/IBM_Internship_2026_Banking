package com.elsys.safebanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TopUpRequest(

        @NotBlank(message = "Account IBAN is required")
        String accountIban,

        /**
         * Amount in minor currency units (cents).
         * E.g. 1000 = €10.00. This is Stripe's native unit.
         */
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        Long amountCents,

        @NotBlank(message = "Currency is required")
        String currency
) {
}
