package com.elsys.safebanking.exception;

public class EPinRateLimitException extends RuntimeException {

    public EPinRateLimitException() {
        super("Too many failed E-PIN attempts. Please wait before trying again.");
    }
}
