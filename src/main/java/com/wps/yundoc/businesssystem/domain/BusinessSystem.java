package com.wps.yundoc.businesssystem.domain;

import java.time.LocalDateTime;

public class BusinessSystem {

    private final String businessSystemId;
    private final String businessSystemName;
    private final String clientId;
    private final String clientSecretDigest;
    private final String clientSecretSalt;
    private final String clientSecretAlg;
    private final String status;
    private final Integer tokenVersion;
    private final Integer permissionVersion;
    private final Integer jwtTtlSeconds;
    private final String description;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public BusinessSystem(
            String businessSystemId,
            String businessSystemName,
            String clientId,
            String clientSecretDigest,
            String clientSecretSalt,
            String clientSecretAlg,
            String status,
            Integer tokenVersion,
            Integer permissionVersion,
            Integer jwtTtlSeconds,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.businessSystemId = businessSystemId;
        this.businessSystemName = businessSystemName;
        this.clientId = clientId;
        this.clientSecretDigest = clientSecretDigest;
        this.clientSecretSalt = clientSecretSalt;
        this.clientSecretAlg = clientSecretAlg;
        this.status = status;
        this.tokenVersion = tokenVersion;
        this.permissionVersion = permissionVersion;
        this.jwtTtlSeconds = jwtTtlSeconds;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getClientSecretDigest() {
        return clientSecretDigest;
    }

    public String getClientSecretSalt() {
        return clientSecretSalt;
    }

    public String getClientSecretAlg() {
        return clientSecretAlg;
    }

    public String getStatus() {
        return status;
    }

    public Integer getTokenVersion() {
        return tokenVersion;
    }

    public Integer getPermissionVersion() {
        return permissionVersion;
    }

    public Integer getJwtTtlSeconds() {
        return jwtTtlSeconds;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
