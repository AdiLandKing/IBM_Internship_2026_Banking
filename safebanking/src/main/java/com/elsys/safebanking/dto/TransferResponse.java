package com.elsys.safebanking.dto;

import com.elsys.safebanking.model.TransactionStatus;

public record TransferResponse(
        Long transactionId,
        TransactionStatus status
) {
}