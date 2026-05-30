package com.wps.yundoc.adminauth.application;

public enum AdminRole {
    SUPER_ADMIN,
    SYSTEM_ADMIN,
    AUDITOR,
    SUPPORT;

    public boolean isSuperAdmin() {
        return this == SUPER_ADMIN;
    }

    public boolean canManageBusinessSystems() {
        return this == SUPER_ADMIN || this == SYSTEM_ADMIN;
    }

    public boolean canViewPermissions() {
        return this == SUPER_ADMIN || this == SYSTEM_ADMIN || this == AUDITOR;
    }

    public boolean canManagePermissions() {
        return this == SUPER_ADMIN || this == SYSTEM_ADMIN;
    }
}
