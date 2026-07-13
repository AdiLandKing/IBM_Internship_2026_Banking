package com.elsys.safebanking.exception;

public class EPinVerificationException extends RuntimeException {

    public EPinVerificationException() {
        super("Current password or E-PIN is incorrect");
    }
}
