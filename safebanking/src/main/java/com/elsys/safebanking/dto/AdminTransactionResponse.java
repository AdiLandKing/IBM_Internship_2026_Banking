package com.elsys.safebanking.dto;

import com.elsys.safebanking.model.BankingTransaction;
import com.elsys.safebanking.model.TransactionStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record AdminTransactionResponse(
        Long transactionId,
        String sourceIban,
        String destinationIban,
        BigDecimal amount,
        String sourceCurrency,
        BigDecimal creditedAmount,
        String destinationCurrency,
        String reason,
        Instant timeStamp,
        TransactionStatus status,
        BigDecimal exchangeRateUsed
) {
    public static AdminTransactionResponse from(BankingTransaction tx) {
        return new AdminTransactionResponse(
                tx.getTranId(),
                tx.getSourceAccount().getIban(),
                tx.getDestinationAccount().getIban(),
                tx.getAmount(),
                tx.getSourceCurrency(),
                tx.getCreditedAmount(),
                tx.getDestinationCurrency(),
                tx.getReason(),
                tx.getTimeStamp(),
                tx.getStatus(),
                tx.getExchangeRateUsed()
        );
    }
}
