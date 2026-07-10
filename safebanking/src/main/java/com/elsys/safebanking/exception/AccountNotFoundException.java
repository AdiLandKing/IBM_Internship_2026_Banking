package com.elsys.safebanking.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String iban) {
        super("Account not found: " + iban);
    }
}
