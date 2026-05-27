package com.wps.yundoc.wpsclient.infrastructure;

import com.wps.yundoc.wpsclient.application.WpsAppToken;
import com.wps.yundoc.wpsclient.application.WpsAppTokenClient;
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

    @Bean
    public WpsAppTokenClient mockWpsAppTokenClient() {
        return this::appToken;
    }

    private WpsPreviewLink previewLink(WpsPreviewRequest request) {
        OffsetDateTime expireAt = OffsetDateTime.now().plusSeconds(request.getExpireSeconds());
        return new WpsPreviewLink("https://preview.test/files/" + request.getFileId(), expireAt);
    }

    private WpsAppToken appToken() {
        return new WpsAppToken("test-wps-app-token", OffsetDateTime.now().plusMinutes(30));
    }
}
