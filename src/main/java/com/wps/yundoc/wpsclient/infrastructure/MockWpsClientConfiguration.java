package com.wps.yundoc.wpsclient.infrastructure;

import com.wps.yundoc.credential.domain.WpsUserToken;
import com.wps.yundoc.wpsclient.application.WpsAppToken;
import com.wps.yundoc.wpsclient.application.WpsAppTokenClient;
import com.wps.yundoc.wpsclient.application.WpsAuthorizationClient;
import com.wps.yundoc.wpsclient.application.WpsFileClient;
import com.wps.yundoc.wpsclient.application.WpsFileItem;
import com.wps.yundoc.wpsclient.application.WpsFileList;
import com.wps.yundoc.wpsclient.application.WpsPreviewClient;
import com.wps.yundoc.wpsclient.application.WpsPreviewLink;
import com.wps.yundoc.wpsclient.application.WpsPreviewRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.OffsetDateTime;
import java.util.Collections;

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

    @Bean
    public WpsFileClient mockWpsFileClient() {
        return request -> new WpsFileList(Collections.singletonList(mockFile()), "next-cursor");
    }

    @Bean
    public WpsAuthorizationClient mockWpsAuthorizationClient() {
        return new WpsAuthorizationClient() {
            @Override
            public String authorizeUrl(String state) {
                return "https://wps.test/oauth/authorize?state=" + state;
            }

            @Override
            public WpsUserToken exchangeCode(String code) {
                return new WpsUserToken("mock-user-access-value", OffsetDateTime.now().plusMinutes(30));
            }
        };
    }

    private WpsPreviewLink previewLink(WpsPreviewRequest request) {
        OffsetDateTime expireAt = OffsetDateTime.now().plusSeconds(request.getExpireSeconds());
        return new WpsPreviewLink("https://preview.test/files/" + request.getFileId(), expireAt);
    }

    private WpsAppToken appToken() {
        return new WpsAppToken("test-wps-app-token", OffsetDateTime.now().plusMinutes(30));
    }

    private WpsFileItem mockFile() {
        return new WpsFileItem("wps-file-001", "demo.docx", "WORD", false, "2026-05-26T18:00:00+08:00");
    }
}
