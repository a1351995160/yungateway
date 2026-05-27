package com.wps.yundoc.capability.userfile.api;

import com.wps.yundoc.wpsclient.application.WpsFileItem;

public class UserFileItemResponse {

    private final String fileId;
    private final String name;
    private final String type;
    private final boolean folder;
    private final String updatedAt;

    public UserFileItemResponse(WpsFileItem item) {
        this.fileId = item.getFileId();
        this.name = item.getName();
        this.type = item.getType();
        this.folder = item.isFolder();
        this.updatedAt = item.getUpdatedAt();
    }

    public String getFileId() {
        return fileId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isFolder() {
        return folder;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
