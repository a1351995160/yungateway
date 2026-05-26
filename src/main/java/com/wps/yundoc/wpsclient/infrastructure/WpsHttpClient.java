package com.wps.yundoc.wpsclient.infrastructure;

import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import com.wps.yundoc.wpsclient.application.WpsPreviewClient;
import com.wps.yundoc.wpsclient.application.WpsPreviewLink;
import com.wps.yundoc.wpsclient.application.WpsPreviewRequest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.Objects;

public class WpsHttpClient implements WpsPreviewClient {

    private final WpsClientProperties properties;
    private final RestTemplate restTemplate;

    public WpsHttpClient(WpsClientProperties properties, RestTemplateBuilder builder) {
        this(properties, builder, restTemplate(properties, builder));
    }

    public WpsHttpClient(
            WpsClientProperties properties,
            RestTemplateBuilder builder,
            RestTemplate restTemplate) {
        Objects.requireNonNull(builder, "builder");
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    public WpsPreviewLink createPreview(WpsPreviewRequest request) {
        WpsPreviewResponse response = executeWithRetry(request);
        return toPreviewLink(response);
    }

    private WpsPreviewResponse executeWithRetry(WpsPreviewRequest request) {
        int maxAttempts = maxAttempts();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return executeOnce(request);
            } catch (ResourceAccessException ex) {
                handleRetry(attempt, maxAttempts, ex);
            } catch (RestClientException ex) {
                throw upstreamError(ex);
            }
        }
        throw upstreamError(null);
    }

    private WpsPreviewResponse executeOnce(WpsPreviewRequest request) {
        HttpEntity<PreviewPayload> entity = httpEntity(request);
        return restTemplate.exchange(
                previewUrl(),
                HttpMethod.POST,
                entity,
                WpsPreviewResponse.class).getBody();
    }

    private WpsPreviewLink toPreviewLink(WpsPreviewResponse response) {
        if (!isSuccess(response)) {
            throw upstreamError(null);
        }
        PreviewData data = response.getData();
        return new WpsPreviewLink(data.getPreviewUrl(), OffsetDateTime.parse(data.getExpireAt()));
    }

    private boolean isSuccess(WpsPreviewResponse response) {
        if (response == null) {
            return false;
        }
        if (response.getCode() == null) {
            return false;
        }
        if (response.getData() == null) {
            return false;
        }
        return response.getCode().intValue() == 0;
    }

    private HttpEntity<PreviewPayload> httpEntity(WpsPreviewRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(request.getAccessToken());
        PreviewPayload payload = new PreviewPayload(request.getFileId(), request.getExpireSeconds());
        return new HttpEntity<>(payload, headers);
    }

    private void handleRetry(int attempt, int maxAttempts, ResourceAccessException ex) {
        if (attempt < maxAttempts) {
            return;
        }
        throw upstreamError(ex);
    }

    private int maxAttempts() {
        return Math.max(1, properties.getMaxRetries());
    }

    private String previewUrl() {
        return properties.getBaseUrl() + properties.getPreviewPath();
    }

    private YundocException upstreamError(Throwable cause) {
        return new YundocException(YundocErrorCode.WPS_UPSTREAM_ERROR, "WPS upstream error", cause);
    }

    private static RestTemplate restTemplate(WpsClientProperties properties, RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(properties.getConnectTimeout())
                .setReadTimeout(properties.getReadTimeout())
                .build();
    }

    private static class PreviewPayload {

        private final String fileId;
        private final int expireSeconds;

        PreviewPayload(String fileId, int expireSeconds) {
            this.fileId = fileId;
            this.expireSeconds = expireSeconds;
        }

        public String getFileId() {
            return fileId;
        }

        public int getExpireSeconds() {
            return expireSeconds;
        }
    }

    public static class WpsPreviewResponse {

        private Integer code;
        private PreviewData data;

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public PreviewData getData() {
            return data;
        }

        public void setData(PreviewData data) {
            this.data = data;
        }
    }

    public static class PreviewData {

        private String previewUrl;
        private String expireAt;

        public String getPreviewUrl() {
            return previewUrl;
        }

        public void setPreviewUrl(String previewUrl) {
            this.previewUrl = previewUrl;
        }

        public String getExpireAt() {
            return expireAt;
        }

        public void setExpireAt(String expireAt) {
            this.expireAt = expireAt;
        }
    }
}
