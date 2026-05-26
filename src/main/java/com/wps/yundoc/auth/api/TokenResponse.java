package com.wps.yundoc.auth.api;

import com.wps.yundoc.auth.application.AuthToken;

import java.util.List;

public class TokenResponse {

    private final String accessToken;
    private final String tokenType = "Bearer";
    private final long expiresIn;
    private final List<String> apiPermissions;

    public TokenResponse(AuthToken token) {
        this.accessToken = token.getAccessToken();
        this.expiresIn = token.getExpiresIn();
        this.apiPermissions = token.getApiPermissions();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public List<String> getApiPermissions() {
        return apiPermissions;
    }
}
