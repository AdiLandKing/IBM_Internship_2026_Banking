package com.elsys.safebanking.exception;

public class ExchangeRateUnavailableException extends RuntimeException {

    public ExchangeRateUnavailableException(String fromCurrency, String toCurrency, Throwable cause) {
        super("Exchange rate unavailable for " + fromCurrency + " → " + toCurrency, cause);
    }

    public ExchangeRateUnavailableException(String fromCurrency, String toCurrency) {
        super("Exchange rate unavailable for " + fromCurrency + " → " + toCurrency);
    }
}
