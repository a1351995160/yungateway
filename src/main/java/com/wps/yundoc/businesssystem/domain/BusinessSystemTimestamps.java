package com.wps.yundoc.businesssystem.domain;

import java.time.LocalDateTime;

public class BusinessSystemTimestamps {

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public BusinessSystemTimestamps(LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
