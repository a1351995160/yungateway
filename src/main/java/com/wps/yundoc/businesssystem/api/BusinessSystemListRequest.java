package com.wps.yundoc.businesssystem.api;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class BusinessSystemListRequest {

    @Size(max = 128)
    private String keyword;

    @Pattern(regexp = "^(ENABLED|DISABLED)?$")
    private String status;

    @Min(1)
    private Integer page = 1;

    @Min(1)
    @Max(100)
    private Integer pageSize = 20;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        if (page != null) {
            this.page = page;
        }
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        if (pageSize != null) {
            this.pageSize = pageSize;
        }
    }

    public int offset() {
        return (page - 1) * pageSize;
    }
}
