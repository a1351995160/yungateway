package com.wps.yundoc.capability.userfile.api;

import java.util.List;

public class UserFileListResponse {

    private final String userId;
    private final List<UserFileResponse> files;

    public UserFileListResponse(String userId, List<UserFileResponse> files) {
        this.userId = userId;
        this.files = files;
    }

    public String getUserId() {
        return userId;
    }

    public List<UserFileResponse> getFiles() {
        return files;
    }
}
