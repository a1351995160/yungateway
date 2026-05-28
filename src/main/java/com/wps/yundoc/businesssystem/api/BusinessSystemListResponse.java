package com.wps.yundoc.businesssystem.api;

import java.util.List;

public class BusinessSystemListResponse {

    private final List<BusinessSystemResponse> items;
    private final boolean hasMore;

    public BusinessSystemListResponse(List<BusinessSystemResponse> items, boolean hasMore) {
        this.items = items;
        this.hasMore = hasMore;
    }

    public List<BusinessSystemResponse> getItems() {
        return items;
    }

    public boolean isHasMore() {
        return hasMore;
    }
}
