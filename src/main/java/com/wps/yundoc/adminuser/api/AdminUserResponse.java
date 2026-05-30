package com.wps.yundoc.adminuser.api;

import com.wps.yundoc.adminauth.application.AdminPrincipal;
import com.wps.yundoc.adminauth.application.AdminRole;
import com.wps.yundoc.adminauth.application.AdminStatus;

import java.time.LocalDateTime;

public class AdminUserResponse {

    private String username;
    private String displayName;
    private AdminRole role;
    private AdminStatus status;
    private boolean superAdmin;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminUserResponse fromPrincipal(AdminPrincipal principal) {
        AdminUserResponse response = new AdminUserResponse();
        response.username = principal.getUsername();
        response.displayName = principal.getDisplayName();
        response.role = principal.getRole();
        response.status = principal.getStatus();
        response.superAdmin = principal.isSuperAdmin();
        response.lastLoginAt = principal.getLastLoginAt();
        return response;
    }

    public static AdminUserResponse databaseAdmin(AdminUserView user) {
        AdminUserResponse response = new AdminUserResponse();
        response.username = user.getUsername();
        response.displayName = user.getDisplayName();
        response.role = user.getRole();
        response.status = user.getStatus();
        response.superAdmin = false;
        response.lastLoginAt = user.getLastLoginAt();
        response.createdAt = user.getCreatedAt();
        response.updatedAt = user.getUpdatedAt();
        return response;
    }

    private AdminUserResponse() {
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

    public interface AdminUserView {

        String getUsername();

        String getDisplayName();

        AdminRole getRole();

        AdminStatus getStatus();

        LocalDateTime getLastLoginAt();

        LocalDateTime getCreatedAt();

        LocalDateTime getUpdatedAt();
    }
}
