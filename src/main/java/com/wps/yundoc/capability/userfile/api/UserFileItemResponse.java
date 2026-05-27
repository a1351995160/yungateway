package com.wps.yundoc.capability.userfile.api;

import com.wps.yundoc.wpsclient.application.WpsFileItem;

public class UserFileItemResponse {

    private final WpsFileItem item;

    public UserFileItemResponse(WpsFileItem item) {
        this.item = item;
    }

    public String getFileId() {
        return item.getFileId();
    }

    public String getName() {
        return item.getName();
    }

    public String getType() {
        return item.getType();
    }

    public boolean isFolder() {
        return item.isFolder();
    }

    public String getUpdatedAt() {
        return item.getUpdatedAt();
    }
}
