package com.wps.yundoc.adminauth.application;

public class AdminJwt {

    private final String token;
    private final long expiresInSeconds;

    public AdminJwt(String token, long expiresInSeconds) {
        this.token = token;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getToken() {
        return token;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}
