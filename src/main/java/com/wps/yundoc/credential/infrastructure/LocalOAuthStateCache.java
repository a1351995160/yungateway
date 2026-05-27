package com.wps.yundoc.credential.infrastructure;

import com.wps.yundoc.credential.domain.OAuthState;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class LocalOAuthStateCache {

    private final ConcurrentMap<String, OAuthState> states = new ConcurrentHashMap<>();

    public void put(OAuthState state) {
        states.put(state.getState(), state);
    }

    public Optional<OAuthState> consume(String state) {
        if (state == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(states.remove(state));
    }
}
