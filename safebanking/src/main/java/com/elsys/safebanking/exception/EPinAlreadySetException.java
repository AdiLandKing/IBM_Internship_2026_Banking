package com.elsys.safebanking.exception;

public class EPinAlreadySetException extends RuntimeException {

    public EPinAlreadySetException() {
        super("E-PIN is already set");
    }
}
