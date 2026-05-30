package com.wps.yundoc.adminauth.api;

import com.wps.yundoc.adminauth.application.AdminJwt;

public class AdminLoginResponse {

    private static final String TOKEN_TYPE = "Bearer";

    private final long expiresInSeconds;

    public AdminLoginResponse(AdminJwt adminJwt) {
        this.expiresInSeconds = adminJwt.getExpiresInSeconds();
    }

    public String getTokenType() {
        return TOKEN_TYPE;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}
