package com.wps.yundoc.businesssystem.api;

public class BusinessSystemSecretResponse {

    private final String businessSystemId;
    private final String clientId;
    private final String clientSecret;
    private final Integer tokenVersion;

    public BusinessSystemSecretResponse(
            String businessSystemId,
            String clientId,
            String clientSecret,
            Integer tokenVersion) {
        this.businessSystemId = businessSystemId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenVersion = tokenVersion;
    }

    public String getBusinessSystemId() {
        return businessSystemId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Integer getTokenVersion() {
        return tokenVersion;
    }
}
