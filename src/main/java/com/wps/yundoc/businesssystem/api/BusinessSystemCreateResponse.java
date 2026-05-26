package com.wps.yundoc.businesssystem.api;

public class BusinessSystemCreateResponse {

    private final BusinessSystemResponse businessSystem;
    private final String clientSecret;

    public BusinessSystemCreateResponse(BusinessSystemResponse businessSystem, String clientSecret) {
        this.businessSystem = businessSystem;
        this.clientSecret = clientSecret;
    }

    public BusinessSystemResponse getBusinessSystem() {
        return businessSystem;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
