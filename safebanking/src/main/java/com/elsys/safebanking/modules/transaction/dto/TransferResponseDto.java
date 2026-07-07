package com.elsys.safebanking.modules.transaction.dto;

public record TransferResponseDto(
        Long transactionId,
        String status
) {
}
