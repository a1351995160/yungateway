package com.wps.yundoc.capability.apppreview.application;

import com.wps.yundoc.capability.apppreview.api.AppPreviewRequest;
import com.wps.yundoc.capability.apppreview.api.AppPreviewResponse;
import com.wps.yundoc.wpsclient.application.WpsPreviewClient;
import org.springframework.stereotype.Service;

@Service
public class AppPreviewService {

    private final WpsPreviewClient previewClient;

    public AppPreviewService(WpsPreviewClient previewClient) {
        this.previewClient = previewClient;
    }

    public AppPreviewResponse create(AppPreviewRequest request) {
        String previewUrl = previewClient.createPreview(request.getFileId());
        return new AppPreviewResponse(request.getFileId(), previewUrl);
    }
}
