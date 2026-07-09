package com.elsys.safebanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank(message = "Source IBAN is required")
        String sourceAccountIban,

        @NotBlank(message = "Destination IBAN is required")
        String destinationAccountIban,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Reason is required")
        String reason,

        @NotBlank(message = "Source currency is required")
        String fromCurrency,

        @NotBlank(message = "Destination currency is required")
        String toCurrency
) {
}