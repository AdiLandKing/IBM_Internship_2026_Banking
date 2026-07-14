package com.elsys.safebanking.dto;

import com.elsys.safebanking.model.TransactionStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionHistoryResponse(
        Long transactionId,
        String sourceIban,
        String destinationIban,
        BigDecimal amount,
        String reason,
        TransactionStatus status,
        Instant timestamp
) {}