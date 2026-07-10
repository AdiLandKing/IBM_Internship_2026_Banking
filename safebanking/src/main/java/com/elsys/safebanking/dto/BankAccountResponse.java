package com.elsys.safebanking.dto;

import com.elsys.safebanking.model.BankAccount;
import java.math.BigDecimal;
import java.time.Instant;

public record BankAccountResponse(
        String iban,
        String name,
        BigDecimal balance,
        String currency,
        Instant createdAt,
        Instant updatedAt
) {
    public static BankAccountResponse from(BankAccount account) {
        return new BankAccountResponse(
                account.getIban(),
                account.getName(),
                account.getBalance(),
                account.getCurrency(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
