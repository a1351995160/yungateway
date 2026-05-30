package com.wps.yundoc.auth.infrastructure;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class LocalUserAssertionNonceCache {

    private final ConcurrentMap<String, Long> nonces = new ConcurrentHashMap<>();
    private final UserAssertionProperties properties;

    public LocalUserAssertionNonceCache(UserAssertionProperties properties) {
        this.properties = properties;
    }

    public boolean markUsed(String businessSystemId, String clientId, String nonce, long epochSeconds) {
        evictExpiredNonces();
        String key = businessSystemId + ":" + clientId + ":" + nonce;
        long expiresAt = Instant.ofEpochSecond(epochSeconds)
                .plus(properties.getTimestampTolerance())
                .toEpochMilli();
        Long existing = nonces.putIfAbsent(key, Long.valueOf(expiresAt));
        if (existing != null) {
            return replaceExpired(key, existing.longValue(), expiresAt);
        }
        if (nonces.size() <= maxNonceCount()) {
            return true;
        }
        nonces.remove(key, Long.valueOf(expiresAt));
        return false;
    }

    private boolean replaceExpired(String key, long existingExpiresAt, long expiresAt) {
        if (existingExpiresAt > Instant.now().toEpochMilli()) {
            return false;
        }
        return nonces.replace(key, Long.valueOf(existingExpiresAt), Long.valueOf(expiresAt));
    }

    private void evictExpiredNonces() {
        long now = Instant.now().toEpochMilli();
        for (Map.Entry<String, Long> entry : nonces.entrySet()) {
            if (entry.getValue().longValue() <= now) {
                nonces.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    private int maxNonceCount() {
        return Math.max(1, properties.getMaxNonceCount());
    }
}
