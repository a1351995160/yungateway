package com.wps.yundoc.credential.infrastructure;

import com.wps.yundoc.credential.domain.WpsUserToken;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class LocalWpsUserTokenCache {

    private final ConcurrentMap<String, WpsUserToken> tokens = new ConcurrentHashMap<>();
    private final WpsCredentialProperties properties;

    public LocalWpsUserTokenCache(WpsCredentialProperties properties) {
        this.properties = properties;
    }

    public Optional<WpsUserToken> get(String userId) {
        WpsUserToken token = tokens.get(userId);
        if (token == null) {
            return Optional.empty();
        }
        return validToken(userId, token);
    }

    public void put(String userId, WpsUserToken token) {
        tokens.put(userId, token);
    }

    private Optional<WpsUserToken> validToken(String userId, WpsUserToken token) {
        if (shouldRefresh(token)) {
            tokens.remove(userId, token);
            return Optional.empty();
        }
        return Optional.of(token);
    }

    private boolean shouldRefresh(WpsUserToken token) {
        OffsetDateTime refreshAt = OffsetDateTime.now().plus(properties.getRefreshSkew());
        return !token.getExpiresAt().isAfter(refreshAt);
    }
}
