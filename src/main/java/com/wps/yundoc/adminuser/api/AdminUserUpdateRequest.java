package com.wps.yundoc.adminuser.api;

import com.wps.yundoc.adminauth.application.AdminRole;
import com.wps.yundoc.adminauth.application.AdminStatus;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AdminUserUpdateRequest {

    @NotBlank
    @Size(max = 128)
    private String displayName;

    @NotNull
    private AdminRole role;

    @NotNull
    private AdminStatus status;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public AdminRole getRole() {
        return role;
    }

    public void setRole(AdminRole role) {
        this.role = role;
    }

    public AdminStatus getStatus() {
        return status;
    }

    public void setStatus(AdminStatus status) {
        this.status = status;
    }
}
