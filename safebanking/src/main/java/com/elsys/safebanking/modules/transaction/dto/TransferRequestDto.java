package com.elsys.safebanking.modules.transaction.dto;

import java.math.BigDecimal;

public record TransferRequestDto(
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        String reason,
        String fromCurrency,
        String toCurrency
) {
}
