package com.elsys.safebanking.dto;

import com.elsys.safebanking.model.BankAccount;

import java.math.BigDecimal;

public record BankAccountResponse(
        Integer accountId,
        String accountName,
        String iban,
        BigDecimal balance,
        String currency
) {
    public static BankAccountResponse from(BankAccount account) {
        return new BankAccountResponse(
                account.getAccountId(),
                account.getAccountName(),
                account.getIban(),
                account.getBalance(),
                account.getCurrency()
        );
    }
}
