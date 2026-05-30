package com.wps.yundoc.common.security;

import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory login attempt limiter.
 *
 * @author wps
 */
@Component
public class AuthenticationAttemptLimiter {

    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final ConcurrentMap<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    public void assertAllowed(String key) {
        String normalizedKey = normalizedKey(key);
        Instant now = Instant.now();
        AttemptWindow window = attempts.get(normalizedKey);
        if (window == null) {
            return;
        }
        if (window.isExpired(now)) {
            attempts.remove(normalizedKey, window);
            return;
        }
        if (window.failures >= MAX_FAILURES) {
            throw new YundocException(YundocErrorCode.TOO_MANY_REQUESTS);
        }
    }

    public void recordSuccess(String key) {
        attempts.remove(normalizedKey(key));
    }

    public void recordFailure(String key) {
        String normalizedKey = normalizedKey(key);
        Instant now = Instant.now();
        AttemptWindow window = attempts.compute(normalizedKey, (ignored, current) -> {
            if (current == null || current.isExpired(now)) {
                return new AttemptWindow(1, now.plus(WINDOW));
            }
            return current.incremented();
        });
        if (window.failures >= MAX_FAILURES) {
            throw new YundocException(YundocErrorCode.TOO_MANY_REQUESTS);
        }
    }

    private String normalizedKey(String key) {
        if (key == null) {
            return "";
        }
        return key.trim().toLowerCase(Locale.ROOT);
    }

    private static class AttemptWindow {
        private final int failures;
        private final Instant expiresAt;

        AttemptWindow(int failures, Instant expiresAt) {
            this.failures = failures;
            this.expiresAt = expiresAt;
        }

        boolean isExpired(Instant now) {
            return !expiresAt.isAfter(now);
        }

        AttemptWindow incremented() {
            return new AttemptWindow(failures + 1, expiresAt);
        }
    }
}
