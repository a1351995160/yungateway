package com.wps.yundoc.businesssystem.domain;

import java.time.LocalDateTime;

public class BusinessSystemApiPermission {

    private final String businessSystemId;
    private final String apiCode;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public BusinessSystemApiPermission(
            String businessSystemId,
            String apiCode,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.businessSystemId = businessSystemId;
        this.apiCode = apiCode;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getBusinessSystemId() {
        return businessSystemId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
