package com.wps.yundoc.businesssystem.api;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class BusinessSystemUpdateRequest {

    @NotBlank
    @Size(max = 128)
    private String businessSystemName;

    @NotBlank
    @Pattern(regexp = "^(ENABLED|DISABLED)$")
    private String status;

    @Min(300)
    @Max(86400)
    @NotNull
    private Integer jwtTtlSeconds;

    @Size(max = 255)
    private String description;

    public String getBusinessSystemName() {
        return businessSystemName;
    }

    public void setBusinessSystemName(String businessSystemName) {
        this.businessSystemName = businessSystemName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
