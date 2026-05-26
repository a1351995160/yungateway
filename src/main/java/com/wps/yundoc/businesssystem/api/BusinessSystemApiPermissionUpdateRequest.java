package com.wps.yundoc.businesssystem.api;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class BusinessSystemApiPermissionUpdateRequest {

    @NotNull
    @Size(max = 20)
    private List<@NotBlank @Size(max = 64) String> apiPermissions;

    public List<String> getApiPermissions() {
        return apiPermissions;
    }

    public void setApiPermissions(List<String> apiPermissions) {
        this.apiPermissions = apiPermissions;
    }
}
