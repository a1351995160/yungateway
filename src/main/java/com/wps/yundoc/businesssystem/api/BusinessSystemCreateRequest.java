package com.wps.yundoc.businesssystem.api;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class BusinessSystemCreateRequest {

    @Size(max = 64)
    @Pattern(regexp = "^[A-Za-z0-9_-]*$")
    private String businessSystemId;

    @NotBlank
    @Size(max = 128)
    private String businessSystemName;

    @Min(300)
    @Max(86400)
    private Integer jwtTtlSeconds = 1800;

    @Size(max = 255)
    private String description;

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
