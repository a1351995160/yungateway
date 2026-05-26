package com.wps.yundoc.businesssystem.domain;

public class BusinessSystemVersioning {

    private final Integer tokenVersion;
    private final Integer permissionVersion;
    private final Integer jwtTtlSeconds;

    public BusinessSystemVersioning(
            Integer tokenVersion,
            Integer permissionVersion,
            Integer jwtTtlSeconds) {
        this.tokenVersion = tokenVersion;
        this.permissionVersion = permissionVersion;
        this.jwtTtlSeconds = jwtTtlSeconds;
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
}
