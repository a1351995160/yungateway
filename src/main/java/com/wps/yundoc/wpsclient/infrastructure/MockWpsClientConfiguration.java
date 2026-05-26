package com.wps.yundoc.wpsclient.infrastructure;

import com.wps.yundoc.wpsclient.application.WpsPreviewClient;
import com.wps.yundoc.wpsclient.application.WpsPreviewLink;
import com.wps.yundoc.wpsclient.application.WpsPreviewRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.OffsetDateTime;

@Configuration
@Profile({"local", "test"})
public class MockWpsClientConfiguration {

    @Bean
    public WpsPreviewClient mockWpsPreviewClient() {
        return this::previewLink;
    }

    private WpsPreviewLink previewLink(WpsPreviewRequest request) {
        OffsetDateTime expireAt = OffsetDateTime.now().plusSeconds(request.getExpireSeconds());
        return new WpsPreviewLink("https://preview.test/files/" + request.getFileId(), expireAt);
    }
}
