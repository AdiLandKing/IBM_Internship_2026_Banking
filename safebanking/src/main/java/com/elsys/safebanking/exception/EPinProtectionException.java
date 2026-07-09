package com.elsys.safebanking.exception;

public class EPinProtectionException extends RuntimeException {

    public EPinProtectionException(Throwable cause) {
        super("Unable to process E-PIN securely", cause);
    }
}
