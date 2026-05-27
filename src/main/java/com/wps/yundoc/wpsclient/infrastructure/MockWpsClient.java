package com.wps.yundoc.wpsclient.infrastructure;

import com.wps.yundoc.capability.userfile.api.UserFileResponse;
import com.wps.yundoc.credential.domain.OAuthState;
import com.wps.yundoc.credential.domain.WpsUserToken;
import com.wps.yundoc.wpsclient.application.WpsAuthorizationClient;
import com.wps.yundoc.wpsclient.application.WpsFileClient;
import com.wps.yundoc.wpsclient.application.WpsPreviewClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Component
@Profile({"local", "test"})
public class MockWpsClient implements WpsPreviewClient, WpsFileClient, WpsAuthorizationClient {

    private static final String MOCK_BASE_URL = "https://mock.wps.local";

    @Override
    public String createPreview(String fileId) {
        return MOCK_BASE_URL + "/previews/" + fileId;
    }

    @Override
    public List<UserFileResponse> listFiles(WpsUserToken token) {
        return Arrays.asList(
                new UserFileResponse("mock-file-1", "MVP Smoke Document.docx"),
                new UserFileResponse("mock-file-2", "MVP Smoke Sheet.xlsx"));
    }

    @Override
    public String authorizeUrl(OAuthState state) {
        return UriComponentsBuilder.fromHttpUrl(MOCK_BASE_URL + "/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("state", state.getState())
                .queryParam("userId", state.getUserId())
                .build()
                .toUriString();
    }

    @Override
    public WpsUserToken exchangeCode(String code, String userId) {
        return new WpsUserToken(userId, "mock-user-token-" + userId, Instant.now().plusSeconds(3600L));
    }
}
