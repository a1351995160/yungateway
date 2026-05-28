package com.wps.yundoc.adminauth.application;

import java.time.LocalDateTime;

public class AdminPrincipal {

    public static final String REQUEST_ATTRIBUTE = AdminPrincipal.class.getName();

    private final String username;
    private final String displayName;
    private final AdminRole role;
    private final AdminStatus status;
    private final LocalDateTime lastLoginAt;

    public AdminPrincipal(
            String username,
            String displayName,
            AdminRole role,
            AdminStatus status,
            LocalDateTime lastLoginAt) {
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.status = status;
        this.lastLoginAt = lastLoginAt;
    }

    public static AdminPrincipal superAdmin(String username) {
        return new AdminPrincipal(username, username, AdminRole.SUPER_ADMIN, AdminStatus.ENABLED, null);
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

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public boolean isSuperAdmin() {
        return role.isSuperAdmin();
    }
}
