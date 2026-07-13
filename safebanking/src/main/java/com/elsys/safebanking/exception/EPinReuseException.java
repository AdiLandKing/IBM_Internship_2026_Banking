package com.elsys.safebanking.exception;

public class EPinReuseException extends RuntimeException {

    public EPinReuseException() {
        super("New E-PIN must be different from the current E-PIN");
    }
}
