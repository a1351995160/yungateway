package com.wps.yundoc.credential.infrastructure;

import com.wps.yundoc.credential.domain.WpsUserToken;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class LocalWpsUserTokenCache {

    private final ConcurrentMap<String, WpsUserToken> tokens = new ConcurrentHashMap<>();
    private final Clock clock = Clock.systemUTC();

    public void put(WpsUserToken token) {
        tokens.put(token.getUserId(), token);
    }

    public Optional<WpsUserToken> get(String userId) {
        WpsUserToken token = tokens.get(userId);
        if (token == null || token.getExpiresAt().isBefore(clock.instant())) {
            tokens.remove(userId);
            return Optional.empty();
        }
        return Optional.of(token);
    }
}
