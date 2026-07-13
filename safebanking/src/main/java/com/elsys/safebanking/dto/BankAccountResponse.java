package com.elsys.safebanking.dto;

import com.elsys.safebanking.model.BankAccount;
import java.math.BigDecimal;

public record BankAccountResponse(
        String iban,
        String name,
        BigDecimal balance,
        String currency
) {
    public static BankAccountResponse from(BankAccount account) {
        return new BankAccountResponse(
                account.getIban(),
                account.getName(),
                account.getBalance(),
                account.getCurrency()
        );
    }
}
