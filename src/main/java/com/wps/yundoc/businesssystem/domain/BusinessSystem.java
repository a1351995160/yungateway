package com.wps.yundoc.businesssystem.domain;

import java.time.LocalDateTime;

public class BusinessSystem {

    private final BusinessSystemProfile profile;
    private final BusinessSystemSecret secret;
    private final BusinessSystemVersioning versioning;
    private final BusinessSystemTimestamps timestamps;

    public BusinessSystem(
            BusinessSystemProfile profile,
            BusinessSystemSecret secret,
            BusinessSystemVersioning versioning,
            BusinessSystemTimestamps timestamps) {
        this.profile = profile;
        this.secret = secret;
        this.versioning = versioning;
        this.timestamps = timestamps;
    }

    public String getBusinessSystemId() {
        return profile.getBusinessSystemId();
    }

    public String getBusinessSystemName() {
        return profile.getBusinessSystemName();
    }

    public String getClientId() {
        return profile.getClientId();
    }

    public String getClientSecretDigest() {
        return secret.getClientSecretDigest();
    }

    public String getClientSecretSalt() {
        return secret.getClientSecretSalt();
    }

    public String getClientSecretAlg() {
        return secret.getClientSecretAlg();
    }

    public String getStatus() {
        return profile.getStatus();
    }

    public Integer getTokenVersion() {
        return versioning.getTokenVersion();
    }

    public Integer getPermissionVersion() {
        return versioning.getPermissionVersion();
    }

    public Integer getJwtTtlSeconds() {
        return versioning.getJwtTtlSeconds();
    }

    public String getDescription() {
        return profile.getDescription();
    }

    public LocalDateTime getCreatedAt() {
        return timestamps.getCreatedAt();
    }

    public LocalDateTime getUpdatedAt() {
        return timestamps.getUpdatedAt();
    }
}
