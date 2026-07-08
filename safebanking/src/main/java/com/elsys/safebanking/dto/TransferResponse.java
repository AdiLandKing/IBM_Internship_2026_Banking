package com.elsys.safebanking.dto;

public record TransferResponseDto(
        Long transactionId,
        String status
) {
}
