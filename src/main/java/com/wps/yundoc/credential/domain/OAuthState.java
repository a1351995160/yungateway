package com.wps.yundoc.credential.domain;

import java.time.Instant;

public class OAuthState {

    private final String state;
    private final String userId;
    private final String businessSystemId;
    private final Instant expiresAt;

    public OAuthState(String state, String userId, String businessSystemId, Instant expiresAt) {
        this.state = state;
        this.userId = userId;
        this.businessSystemId = businessSystemId;
        this.expiresAt = expiresAt;
    }

    public String getState() {
        return state;
    }

    public String getUserId() {
        return userId;
    }

    public String getBusinessSystemId() {
        return businessSystemId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
