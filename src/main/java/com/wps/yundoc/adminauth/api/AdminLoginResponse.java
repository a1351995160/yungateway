package com.wps.yundoc.adminauth.api;

import com.wps.yundoc.adminauth.application.AdminJwt;

public class AdminLoginResponse {

    private final String tokenType = "Bearer";
    private final String adminJwt;
    private final long expiresInSeconds;

    public AdminLoginResponse(AdminJwt adminJwt) {
        this.adminJwt = adminJwt.getToken();
        this.expiresInSeconds = adminJwt.getExpiresInSeconds();
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAdminJwt() {
        return adminJwt;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}
