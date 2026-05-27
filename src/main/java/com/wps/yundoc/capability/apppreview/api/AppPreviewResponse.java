package com.wps.yundoc.capability.apppreview.api;

public class AppPreviewResponse {

    private final String fileId;
    private final String previewUrl;

    public AppPreviewResponse(String fileId, String previewUrl) {
        this.fileId = fileId;
        this.previewUrl = previewUrl;
    }

    public String getFileId() {
        return fileId;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }
}
