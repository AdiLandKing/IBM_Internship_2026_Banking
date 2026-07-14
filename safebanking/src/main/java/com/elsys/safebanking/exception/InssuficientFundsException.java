package com.elsys.safebanking.exception;

public class InssuficientFundsException extends RuntimeException {
    public InssuficientFundsException(String message) {
        super(message);
    }

    public InssuficientFundsException() {
        super("Insufficient funds");
    }
}
