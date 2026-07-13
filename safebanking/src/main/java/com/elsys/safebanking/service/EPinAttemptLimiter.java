package com.elsys.safebanking.service;

import com.elsys.safebanking.exception.EPinRateLimitException;
import com.elsys.safebanking.validation.EPinPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class EPinAttemptLimiter {

    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(EPinPolicy.LOCKOUT_MINUTES);

    private final ConcurrentMap<String, FailureWindow> failures = new ConcurrentHashMap<>();

    public void checkAllowed(Long userId, String clientIp, String action) {
        FailureWindow window = failures.get(key(userId, clientIp, action));
        if (window == null) {
            return;
        }
        if (window.expiresAt().isBefore(Instant.now())) {
            failures.remove(key(userId, clientIp, action), window);
            return;
        }
        if (window.count() >= EPinPolicy.MAX_FAILED_ATTEMPTS) {
            throw new EPinRateLimitException();
        }
    }

    public void recordFailure(Long userId, String clientIp, String action) {
        failures.compute(key(userId, clientIp, action), (ignored, current) -> {
            Instant now = Instant.now();
            if (current == null || current.expiresAt().isBefore(now)) {
                return new FailureWindow(1, now.plus(LOCKOUT_DURATION));
            }
            return new FailureWindow(current.count() + 1, now.plus(LOCKOUT_DURATION));
        });
    }

    public void recordSuccess(Long userId, String clientIp, String action) {
        failures.remove(key(userId, clientIp, action));
    }

    private String key(Long userId, String clientIp, String action) {
        return "%s:%s:%s".formatted(userId, Objects.toString(clientIp, "unknown"), action);
    }

    private record FailureWindow(int count, Instant expiresAt) {
    }
}
