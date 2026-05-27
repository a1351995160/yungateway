package com.wps.yundoc.capability.apppreview.api;

import javax.validation.constraints.NotBlank;

public class AppPreviewRequest {

    @NotBlank
    private String fileId;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
