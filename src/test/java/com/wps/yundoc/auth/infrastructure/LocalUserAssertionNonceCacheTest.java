package com.wps.yundoc.auth.infrastructure;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LocalUserAssertionNonceCacheTest {

    @Test
    void rejectsImmediateReplayInSameBusinessClientWindow() {
        LocalUserAssertionNonceCache cache = new LocalUserAssertionNonceCache(properties(10, 10));
        long timestamp = Instant.now().getEpochSecond();

        assertThat(cache.markUsed("biz-001", "client-001", "nonce-001", timestamp)).isTrue();
        assertThat(cache.markUsed("biz-001", "client-001", "nonce-001", timestamp)).isFalse();
    }

    @Test
    void isolatesNonceByBusinessSystemAndClient() {
        LocalUserAssertionNonceCache cache = new LocalUserAssertionNonceCache(properties(10, 10));
        long timestamp = Instant.now().getEpochSecond();

        assertThat(cache.markUsed("biz-001", "client-001", "nonce-001", timestamp)).isTrue();
        assertThat(cache.markUsed("biz-002", "client-001", "nonce-001", timestamp)).isTrue();
        assertThat(cache.markUsed("biz-001", "client-002", "nonce-001", timestamp)).isTrue();
    }

    @Test
    void allowsReuseAfterSignedTimestampWindowExpires() {
        LocalUserAssertionNonceCache cache = new LocalUserAssertionNonceCache(properties(1, 10));
        long expiredTimestamp = Instant.now().minusSeconds(5).getEpochSecond();
        long currentTimestamp = Instant.now().getEpochSecond();

        assertThat(cache.markUsed("biz-001", "client-001", "nonce-001", expiredTimestamp)).isTrue();
        assertThat(cache.markUsed("biz-001", "client-001", "nonce-001", currentTimestamp)).isTrue();
    }

    @Test
    void rejectsNewNonceWhenFullOfActiveNonces() {
        LocalUserAssertionNonceCache cache = new LocalUserAssertionNonceCache(properties(60, 1));
        long timestamp = Instant.now().getEpochSecond();

        assertThat(cache.markUsed("biz-001", "client-001", "nonce-001", timestamp)).isTrue();
        assertThat(cache.markUsed("biz-001", "client-001", "nonce-002", timestamp)).isFalse();
        assertThat(cache.markUsed("biz-001", "client-001", "nonce-001", timestamp)).isFalse();
    }

    private UserAssertionProperties properties(int toleranceSeconds, int maxNonceCount) {
        UserAssertionProperties properties = new UserAssertionProperties();
        properties.setSecret("test-user-assertion-secret-with-enough-length");
        properties.setTimestampTolerance(Duration.ofSeconds(toleranceSeconds));
        properties.setMaxNonceCount(maxNonceCount);
        return properties;
    }
}
