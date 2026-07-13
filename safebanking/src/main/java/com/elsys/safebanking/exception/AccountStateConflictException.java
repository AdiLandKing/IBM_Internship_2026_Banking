package com.elsys.safebanking.exception;

public class AccountStateConflictException extends RuntimeException {
    public AccountStateConflictException(String message) {
        super(message);
    }
}
