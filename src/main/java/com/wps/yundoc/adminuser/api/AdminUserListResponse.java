package com.wps.yundoc.adminuser.api;

import java.util.List;

public class AdminUserListResponse {

    private final List<AdminUserResponse> items;
    private final boolean hasMore;

    public AdminUserListResponse(List<AdminUserResponse> items, boolean hasMore) {
        this.items = items;
        this.hasMore = hasMore;
    }

    public List<AdminUserResponse> getItems() {
        return items;
    }

    public boolean isHasMore() {
        return hasMore;
    }
}
