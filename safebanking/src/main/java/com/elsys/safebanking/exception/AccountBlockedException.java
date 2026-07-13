package com.elsys.safebanking.exception;

public class AccountBlockedException extends RuntimeException {

    public AccountBlockedException(String iban) {
        super("Account " + iban + " has been blocked by an administrator and cannot be modified by the user");
    }
}
