package com.elsys.safebanking.dto;

public record TransferResponse(
        Long transactionId,
        String status
) {
}
