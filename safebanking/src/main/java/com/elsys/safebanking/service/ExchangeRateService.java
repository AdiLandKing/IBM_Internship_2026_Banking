package com.elsys.safebanking.service;

import com.elsys.safebanking.exception.ExchangeRateUnavailableException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExchangeRateService {

    private static final String FRANKFURTER_URL = "https://api.frankfurter.app";

    private final RestClient restClient;
    private final long cacheTtlSeconds;

    /** Cached entry: the rate and the time it was fetched. */
    private record CacheEntry(BigDecimal rate, Instant fetchedAt) {}

    /**
     * Typed projection of the Frankfurter /latest response.
     * Jackson maps the "rates" object directly to {@code Map<String, BigDecimal>},
     * avoiding the precision loss that occurs when numbers are first parsed as Double.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record FrankfurterResponse(Map<String, BigDecimal> rates) {}

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public ExchangeRateService(
            @Value("${app.fx.cache-ttl-seconds:300}") long cacheTtlSeconds) {
        this.cacheTtlSeconds = cacheTtlSeconds;
        this.restClient = RestClient.builder()
                .baseUrl(FRANKFURTER_URL)
                .build();
    }

    /**
     * Returns the exchange rate from {@code fromCurrency} to {@code toCurrency}.
     * Returns {@link BigDecimal#ONE} immediately when both currencies are the same.
     * Results are cached for {@code app.fx.cache-ttl-seconds} seconds.
     *
     * @throws ExchangeRateUnavailableException if the Frankfurter API is unreachable
     *         or the currency pair is unknown.
     */
    public BigDecimal getRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return BigDecimal.ONE;
        }

        String key = fromCurrency.toUpperCase() + "_" + toCurrency.toUpperCase();

        CacheEntry cached = cache.get(key);
        if (cached != null && !isStale(cached)) {
            return cached.rate();
        }

        BigDecimal rate = fetchRate(fromCurrency.toUpperCase(), toCurrency.toUpperCase());
        cache.put(key, new CacheEntry(rate, Instant.now()));
        return rate;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private boolean isStale(CacheEntry entry) {
        return Instant.now().isAfter(entry.fetchedAt().plusSeconds(cacheTtlSeconds));
    }

    /**
     * Calls GET /latest?from={from}&to={to} and extracts the single rate.
     * Frankfurter response shape:
     * <pre>
     * {
     *   "amount": 1.0,
     *   "base": "EUR",
     *   "date": "2025-01-01",
     *   "rates": { "USD": 1.0823 }
     * }
     * </pre>
     */
    private BigDecimal fetchRate(String from, String to) {
        try {
            FrankfurterResponse response = restClient.get()
                    .uri("/latest?from={from}&to={to}", from, to)
                    .retrieve()
                    .body(FrankfurterResponse.class);

            if (response == null || response.rates() == null || !response.rates().containsKey(to)) {
                throw new ExchangeRateUnavailableException(from, to);
            }

            return response.rates().get(to);

        } catch (RestClientException ex) {
            throw new ExchangeRateUnavailableException(from, to, ex);
        }
    }
}
