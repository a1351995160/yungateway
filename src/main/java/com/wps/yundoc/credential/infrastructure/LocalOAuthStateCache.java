package com.wps.yundoc.credential.infrastructure;

import com.wps.yundoc.credential.domain.OAuthState;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class LocalOAuthStateCache {

    private final ConcurrentMap<String, OAuthState> states = new ConcurrentHashMap<>();

    public void put(OAuthState state) {
        states.put(state.getState(), state);
    }

    public Optional<OAuthState> take(String stateValue) {
        OAuthState state = states.remove(stateValue);
        if (state == null) {
            return Optional.empty();
        }
        return validState(state);
    }

    private Optional<OAuthState> validState(OAuthState state) {
        if (state.getExpiresAt().isBefore(OffsetDateTime.now())) {
            return Optional.empty();
        }
        return Optional.of(state);
    }
}
