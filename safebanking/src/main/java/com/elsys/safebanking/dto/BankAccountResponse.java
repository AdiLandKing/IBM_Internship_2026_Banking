package com.elsys.safebanking.dto;

import com.elsys.safebanking.model.BankAccount;

public record BankAccountResponse(
        Integer accountId,
        String accountName,
        String iban,
        Integer balance,
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
