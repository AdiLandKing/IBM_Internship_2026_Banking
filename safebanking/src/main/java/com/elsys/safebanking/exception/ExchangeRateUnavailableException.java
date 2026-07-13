package com.elsys.safebanking.exception;

public class ExchangeRateUnavailableException extends RuntimeException {

    /** Machine-readable codes that let clients and operators distinguish failure modes. */
    public enum ErrorCode {
        /** The upstream HTTP call failed (network timeout, connection refused, etc.). */
        UPSTREAM_ERROR,
        /** The API responded successfully but did not include the requested currency pair. */
        RATE_NOT_IN_RESPONSE,
        /** The API returned a rate that is null, zero, or negative. */
        INVALID_RATE
    }

    private final ErrorCode errorCode;

    /** Used when the root cause is a network / HTTP failure from RestClient. */
    public ExchangeRateUnavailableException(String fromCurrency, String toCurrency, Throwable cause) {
        super("Exchange rate unavailable for " + fromCurrency + " → " + toCurrency, cause);
        this.errorCode = ErrorCode.UPSTREAM_ERROR;
    }

    /** Used when the response is structurally wrong or the rate value is invalid. */
    public ExchangeRateUnavailableException(String fromCurrency, String toCurrency, ErrorCode errorCode) {
        super("Exchange rate unavailable for " + fromCurrency + " → " + toCurrency
                + " [" + errorCode + "]");
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
