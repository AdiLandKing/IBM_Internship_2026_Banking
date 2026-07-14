package com.elsys.safebanking.service;

import com.elsys.safebanking.exception.ExchangeRateUnavailableException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

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

    private final ConcurrentHashMap<String, CacheEntry>    cache = new ConcurrentHashMap<>();

    /**
     * Per-key locks used to guarantee at most one in-flight HTTP fetch per currency pair.
     * Threads that arrive while a fetch is already in progress for the same key will block
     * on the lock and then read the freshly-written cache entry instead of issuing a second
     * upstream request (thundering-herd prevention).
     */
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Autowired
    public ExchangeRateService(
            @Value("${app.fx.cache-ttl-seconds:300}") long cacheTtlSeconds,
            @Value("${app.fx.connect-timeout-seconds:5}") int connectTimeoutSeconds,
            @Value("${app.fx.read-timeout-seconds:5}") int readTimeoutSeconds) {
        this(RestClient.builder()
                        .baseUrl(FRANKFURTER_URL)
                        .requestFactory(buildRequestFactory(connectTimeoutSeconds, readTimeoutSeconds)),
                cacheTtlSeconds);
    }

    /** Package-private constructor used by tests to inject a MockRestServiceServer-bound builder. */
    ExchangeRateService(RestClient.Builder builder, long cacheTtlSeconds) {
        this.cacheTtlSeconds = cacheTtlSeconds;
        this.restClient      = builder.build();
    }

    /**
     * Returns the exchange rate from {@code fromCurrency} to {@code toCurrency}.
     * Returns {@link BigDecimal#ONE} immediately when both currencies are the same.
     * Results are cached for {@code app.fx.cache-ttl-seconds} seconds.
     * Only one upstream HTTP request is ever in flight per currency pair at a time.
     *
     * @throws ExchangeRateUnavailableException if the Frankfurter API is unreachable,
     *         the currency pair is unknown, or the returned rate is invalid.
     */
    public BigDecimal getRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return BigDecimal.ONE;
        }

        String from = fromCurrency.toUpperCase();
        String to   = toCurrency.toUpperCase();
        String key  = from + "_" + to;

        // Fast path — valid cached entry requires no locking.
        CacheEntry cached = cache.get(key);
        if (cached != null && !isStale(cached)) {
            log.debug("FX cache hit pair={} rate={} cachedAt={}", key, cached.rate(), cached.fetchedAt());
            return cached.rate();
        }

        // Slow path — acquire a per-key lock so only one thread fetches upstream.
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            // Re-check under the lock: a concurrent thread may have already refreshed the entry.
            CacheEntry reChecked = cache.get(key);
            if (reChecked != null && !isStale(reChecked)) {
                log.debug("FX cache hit (post-lock) pair={} rate={} cachedAt={}",
                        key, reChecked.rate(), reChecked.fetchedAt());
                return reChecked.rate();
            }

            if (reChecked != null) {
                log.debug("FX cache stale pair={} cachedAt={} — re-fetching", key, reChecked.fetchedAt());
            } else {
                log.debug("FX cache miss pair={} — fetching from Frankfurter", key);
            }

            BigDecimal rate = fetchRate(from, to);
            cache.put(key, new CacheEntry(rate, Instant.now()));
            log.info("FX rate fetched pair={} rate={}", key, rate);
            return rate;
        } finally {
            lock.unlock();
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private static SimpleClientHttpRequestFactory buildRequestFactory(int connectSeconds, int readSeconds) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(connectSeconds));
        factory.setReadTimeout(Duration.ofSeconds(readSeconds));
        return factory;
    }

    private boolean isStale(CacheEntry entry) {
        return Instant.now().isAfter(entry.fetchedAt().plusSeconds(cacheTtlSeconds));
    }

    /**
     * Calls GET /latest?from={from}&to={to}, extracts the rate, and validates it.
     * Frankfurter response shape:
     * <pre>
     * {
     *   "amount": 1.0,
     *   "base": "EUR",
     *   "date": "2025-01-01",
     *   "rates": { "USD": 1.0823 }
     * }
     * </pre>
     *
     * @throws ExchangeRateUnavailableException on network failure, missing key, or invalid rate.
     */
    private BigDecimal fetchRate(String from, String to) {
        try {
            FrankfurterResponse response = restClient.get()
                    .uri("/latest?from={from}&to={to}", from, to)
                    .retrieve()
                    .body(FrankfurterResponse.class);

            if (response == null || response.rates() == null || !response.rates().containsKey(to)) {
                log.warn("FX fetch returned no rate pair={}_{} response={}", from, to, response);
                throw new ExchangeRateUnavailableException(from, to,
                        ExchangeRateUnavailableException.ErrorCode.RATE_NOT_IN_RESPONSE);
            }

            BigDecimal rate = response.rates().get(to);
            if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("FX fetch returned invalid rate pair={}_{} rate={}", from, to, rate);
                throw new ExchangeRateUnavailableException(from, to,
                        ExchangeRateUnavailableException.ErrorCode.INVALID_RATE);
            }

            return rate;

        } catch (RestClientException ex) {
            log.error("FX fetch failed pair={}_{} error={}", from, to, ex.getMessage(), ex);
            throw new ExchangeRateUnavailableException(from, to, ex);
        }
    }
}
