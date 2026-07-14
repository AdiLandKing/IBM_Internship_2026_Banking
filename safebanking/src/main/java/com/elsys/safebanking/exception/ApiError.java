package com.elsys.safebanking.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String errorCode,
        Map<String, String> fieldErrors
) {
    public static ApiError of(int status, String error, String message) {
        return new ApiError(Instant.now(), status, error, message, null, Map.of());
    }

    public static ApiError withErrorCode(int status, String error, String message, String errorCode) {
        return new ApiError(Instant.now(), status, error, message, errorCode, Map.of());
    }

    public static ApiError withFields(int status, String error, String message, Map<String, String> fieldErrors) {
        return new ApiError(Instant.now(), status, error, message, null, fieldErrors);
    }
}
