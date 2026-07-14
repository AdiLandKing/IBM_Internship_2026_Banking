package com.elsys.safebanking.dto;

import com.elsys.safebanking.model.BankAccount;

public record RecipientAccountResponse(
        String iban,
        String currency
) {
    public static RecipientAccountResponse from(BankAccount account) {
        return new RecipientAccountResponse(account.getIban(), account.getCurrency());
    }
}
