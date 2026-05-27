package com.wps.yundoc.credential.domain;

import java.time.Instant;

public class WpsUserToken {

    private final String userId;
    private final String accessToken;
    private final Instant expiresAt;

    public WpsUserToken(String userId, String accessToken, Instant expiresAt) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
