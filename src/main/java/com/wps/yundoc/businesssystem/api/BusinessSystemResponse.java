package com.wps.yundoc.businesssystem.api;

import java.time.LocalDateTime;
import java.util.List;

public class BusinessSystemResponse {

    private String businessSystemId;
    private String businessSystemName;
    private String clientId;
    private String status;
    private Integer tokenVersion;
    private Integer permissionVersion;
    private Integer jwtTtlSeconds;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> apiPermissions;

    public String getBusinessSystemId() {
        return businessSystemId;
    }

    public void setBusinessSystemId(String businessSystemId) {
        this.businessSystemId = businessSystemId;
    }

    public String getBusinessSystemName() {
        return businessSystemName;
    }

    public void setBusinessSystemName(String businessSystemName) {
        this.businessSystemName = businessSystemName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTokenVersion() {
        return tokenVersion;
    }

    public void setTokenVersion(Integer tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

    public Integer getPermissionVersion() {
        return permissionVersion;
    }

    public void setPermissionVersion(Integer permissionVersion) {
        this.permissionVersion = permissionVersion;
    }

    public Integer getJwtTtlSeconds() {
        return jwtTtlSeconds;
    }

    public void setJwtTtlSeconds(Integer jwtTtlSeconds) {
        this.jwtTtlSeconds = jwtTtlSeconds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getApiPermissions() {
        return apiPermissions;
    }

    public void setApiPermissions(List<String> apiPermissions) {
        this.apiPermissions = apiPermissions;
    }
}
