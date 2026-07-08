package com.elsys.safebanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TransferRequest(
    @NotNull(message = "Source account ID is required")
    Long sourceAccountId,

    @NotNull(message = "Destination account ID is required")
    Long destinationAccountId,

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    BigDecimal amount,

    String reason,

    @NotBlank(message = "Source currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    String fromCurrency,

    @NotBlank(message = "Destination currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    String toCurrency
) {
}