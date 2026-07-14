package com.elsys.safebanking.service;

import com.elsys.safebanking.exception.ExchangeRateUnavailableException;
import com.elsys.safebanking.exception.ExchangeRateUnavailableException.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ExchangeRateServiceTest {

    private static final String BASE_URL = "https://api.frankfurter.app";

    private MockRestServiceServer mockServer;
    private ExchangeRateService   service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        // 1-second TTL so staleness can be tested by manipulating the cache directly
        service = new ExchangeRateService(builder, 1L);
    }

    // -------------------------------------------------------------------------
    // Same-currency short-circuit
    // -------------------------------------------------------------------------

    @Test
    void getRate_sameCurrency_returnsBigDecimalOneWithoutHttpCall() {
        BigDecimal rate = service.getRate("EUR", "EUR");

        assertThat(rate).isEqualByComparingTo(BigDecimal.ONE);
        mockServer.verify(); // no HTTP calls expected
    }

    @Test
    void getRate_sameCurrencyMixedCase_returnsBigDecimalOneWithoutHttpCall() {
        BigDecimal rate = service.getRate("eur", "EUR");

        assertThat(rate).isEqualByComparingTo(BigDecimal.ONE);
        mockServer.verify();
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void getRate_firstCall_fetchesFromApiAndReturnsRate() {
        mockServer.expect(requestTo(BASE_URL + "/latest?from=EUR&to=USD"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {"amount":1.0,"base":"EUR","date":"2025-01-01","rates":{"USD":1.0823}}
                        """,
                        MediaType.APPLICATION_JSON
                ));

        BigDecimal rate = service.getRate("EUR", "USD");

        assertThat(rate).isEqualByComparingTo("1.0823");
        mockServer.verify();
    }

    // -------------------------------------------------------------------------
    // Cache behaviour
    // -------------------------------------------------------------------------

    @Test
    void getRate_secondCall_returnsCachedRateWithoutHttpCall() {
        mockServer.expect(requestTo(BASE_URL + "/latest?from=EUR&to=USD"))
                .andRespond(withSuccess(
                        """
                        {"amount":1.0,"base":"EUR","date":"2025-01-01","rates":{"USD":1.0823}}
                        """,
                        MediaType.APPLICATION_JSON
                ));

        service.getRate("EUR", "USD"); // first call — fetches
        BigDecimal cached = service.getRate("EUR", "USD"); // second call — from cache

        assertThat(cached).isEqualByComparingTo("1.0823");
        mockServer.verify(); // only one HTTP call was made
    }

    @Test
    void getRate_afterTtlExpiry_refetchesFromApi() throws InterruptedException {
        String body = """
                {"amount":1.0,"base":"EUR","date":"2025-01-01","rates":{"USD":1.0823}}
                """;
        // TTL is 1 s — expect two fetches
        mockServer.expect(requestTo(BASE_URL + "/latest?from=EUR&to=USD"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(BASE_URL + "/latest?from=EUR&to=USD"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        service.getRate("EUR", "USD");           // populates cache
        Thread.sleep(1_100);                      // wait for the 1-s TTL to expire
        service.getRate("EUR", "USD");           // stale → re-fetch

        mockServer.verify();
    }

    // -------------------------------------------------------------------------
    // Thundering-herd — concurrent callers for the same stale/missing key
    // -------------------------------------------------------------------------

    @Test
    void getRate_concurrentCallsForSameKey_onlyOneHttpRequestIsMade() throws InterruptedException {
        AtomicInteger requestCount = new AtomicInteger();

        // Only register a single expected call — MockRestServiceServer will throw if more arrive.
        mockServer.expect(requestTo(BASE_URL + "/latest?from=EUR&to=GBP"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    requestCount.incrementAndGet();
                    try { Thread.sleep(20); } catch (InterruptedException ignored) {}
                    return withSuccess(
                            """
                            {"amount":1.0,"base":"EUR","date":"2025-01-01","rates":{"GBP":0.8612}}
                            """,
                            MediaType.APPLICATION_JSON
                    ).createResponse(request);
                });

        int threads = 8;
        CountDownLatch start  = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            Thread.ofVirtual().start(() -> {
                try {
                    start.await();
                    service.getRate("EUR", "GBP");
                } catch (InterruptedException ignored) {
                } finally {
                    finish.countDown();
                }
            });
        }

        start.countDown();
        finish.await();

        assertThat(requestCount.get()).isEqualTo(1);
        mockServer.verify();
    }

    // -------------------------------------------------------------------------
    // Error cases
    // -------------------------------------------------------------------------

    @Test
    void getRate_serverError_throwsExchangeRateUnavailableWithUpstreamCode() {
        mockServer.expect(requestTo(BASE_URL + "/latest?from=EUR&to=USD"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> service.getRate("EUR", "USD"))
                .isInstanceOf(ExchangeRateUnavailableException.class)
                .satisfies(ex -> assertThat(((ExchangeRateUnavailableException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.UPSTREAM_ERROR));
    }

    @Test
    void getRate_rateKeyMissingFromResponse_throwsRateNotInResponse() {
        mockServer.expect(requestTo(BASE_URL + "/latest?from=EUR&to=USD"))
                .andRespond(withSuccess(
                        // "USD" key absent — Frankfurter returns this for unknown currencies
                        """
                        {"amount":1.0,"base":"EUR","date":"2025-01-01","rates":{}}
                        """,
                        MediaType.APPLICATION_JSON
                ));

        assertThatThrownBy(() -> service.getRate("EUR", "USD"))
                .isInstanceOf(ExchangeRateUnavailableException.class)
                .satisfies(ex -> assertThat(((ExchangeRateUnavailableException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.RATE_NOT_IN_RESPONSE));
    }

    @Test
    void getRate_zeroRate_throwsInvalidRate() {
        mockServer.expect(requestTo(BASE_URL + "/latest?from=EUR&to=USD"))
                .andRespond(withSuccess(
                        """
                        {"amount":1.0,"base":"EUR","date":"2025-01-01","rates":{"USD":0}}
                        """,
                        MediaType.APPLICATION_JSON
                ));

        assertThatThrownBy(() -> service.getRate("EUR", "USD"))
                .isInstanceOf(ExchangeRateUnavailableException.class)
                .satisfies(ex -> assertThat(((ExchangeRateUnavailableException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_RATE));
    }

    @Test
    void getRate_negativeRate_throwsInvalidRate() {
        mockServer.expect(requestTo(BASE_URL + "/latest?from=EUR&to=USD"))
                .andRespond(withSuccess(
                        """
                        {"amount":1.0,"base":"EUR","date":"2025-01-01","rates":{"USD":-1.5}}
                        """,
                        MediaType.APPLICATION_JSON
                ));

        assertThatThrownBy(() -> service.getRate("EUR", "USD"))
                .isInstanceOf(ExchangeRateUnavailableException.class)
                .satisfies(ex -> assertThat(((ExchangeRateUnavailableException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_RATE));
    }

    @Test
    void getRate_failedFetchIsNotCached_nextCallRetries() {
        mockServer.expect(requestTo(BASE_URL + "/latest?from=EUR&to=USD"))
                .andRespond(withServerError());
        mockServer.expect(requestTo(BASE_URL + "/latest?from=EUR&to=USD"))
                .andRespond(withSuccess(
                        """
                        {"amount":1.0,"base":"EUR","date":"2025-01-01","rates":{"USD":1.0823}}
                        """,
                        MediaType.APPLICATION_JSON
                ));

        // First call fails — must not poison the cache
        assertThatThrownBy(() -> service.getRate("EUR", "USD"))
                .isInstanceOf(ExchangeRateUnavailableException.class);

        // Second call succeeds
        BigDecimal rate = service.getRate("EUR", "USD");
        assertThat(rate).isEqualByComparingTo("1.0823");

        mockServer.verify();
    }
}
