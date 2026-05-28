package com.wps.yundoc.adminauth.application;

public enum AdminRole {
    SUPER_ADMIN,
    SYSTEM_ADMIN,
    AUDITOR,
    SUPPORT;

    public boolean isSuperAdmin() {
        return this == SUPER_ADMIN;
    }
}
