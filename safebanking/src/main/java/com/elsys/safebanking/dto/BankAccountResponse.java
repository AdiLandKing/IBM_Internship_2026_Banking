package com.elsys.safebanking.dto;

import com.elsys.safebanking.model.BankAccount;

public record BankAccountResponse(
        Integer accountId,
        String accountName,
        Integer balance,
        String currency,
        Long userId
) {
    public static BankAccountResponse from(BankAccount account) {
        return new BankAccountResponse(
                account.getAccountId(),
                account.getAccountName(),
                account.getBalance(),
                account.getCurrency(),
                account.getOwner().getId()
        );
    }
}
