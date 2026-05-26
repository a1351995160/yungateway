package com.wps.yundoc.businesssystem.domain;

public class BusinessSystemProfile {

    private final String businessSystemId;
    private final String businessSystemName;
    private final String clientId;
    private final String status;
    private final String description;

    public BusinessSystemProfile(
            String businessSystemId,
            String businessSystemName,
            String clientId,
            String status,
            String description) {
        this.businessSystemId = businessSystemId;
        this.businessSystemName = businessSystemName;
        this.clientId = clientId;
        this.status = status;
        this.description = description;
    }

    public String getBusinessSystemId() {
        return businessSystemId;
    }

    public String getBusinessSystemName() {
        return businessSystemName;
    }

    public String getClientId() {
        return clientId;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }
}
