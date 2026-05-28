package com.wps.yundoc.adminuser.api;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class AdminUserListRequest {

    @Size(max = 64)
    private String keyword;

    @Pattern(regexp = "^(|ENABLED|DISABLED)$")
    private String status;

    @Pattern(regexp = "^(|SYSTEM_ADMIN|AUDITOR|SUPPORT)$")
    private String role;

    @Min(1)
    private int page = 1;

    @Min(1)
    @Max(100)
    private int pageSize = 20;

    public String getKeyword() {
        return blankToNull(keyword);
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStatus() {
        return blankToNull(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return blankToNull(role);
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
