package com.wps.yundoc.adminuser.api;

import com.wps.yundoc.adminauth.application.AdminRole;
import com.wps.yundoc.adminauth.application.AdminStatus;

import java.time.LocalDateTime;

public class AdminUserResponse {

    private final String username;
    private final String displayName;
    private final AdminRole role;
    private final AdminStatus status;
    private final boolean superAdmin;
    private final LocalDateTime lastLoginAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AdminUserResponse(
            String username,
            String displayName,
            AdminRole role,
            AdminStatus status,
            boolean superAdmin,
            LocalDateTime lastLoginAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.status = status;
        this.superAdmin = superAdmin;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public AdminRole getRole() {
        return role;
    }

    public AdminStatus getStatus() {
        return status;
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
