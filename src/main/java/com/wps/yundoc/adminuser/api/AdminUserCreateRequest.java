package com.wps.yundoc.adminuser.api;

import com.wps.yundoc.adminauth.application.AdminRole;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class AdminUserCreateRequest {

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_.-]{3,64}$")
    private String username;

    @NotBlank
    @Size(max = 128)
    private String displayName;

    @NotNull
    private AdminRole role;

    @NotBlank
    @Size(min = 8, max = 128)
    private String initialPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

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

    public String getInitialPassword() {
        return initialPassword;
    }

    public void setInitialPassword(String initialPassword) {
        this.initialPassword = initialPassword;
    }
}
