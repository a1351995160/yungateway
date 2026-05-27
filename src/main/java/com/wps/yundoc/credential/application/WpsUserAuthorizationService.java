package com.wps.yundoc.credential.application;

import com.wps.yundoc.common.context.RequestContext;
import com.wps.yundoc.common.context.RequestContextHolder;
import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import com.wps.yundoc.credential.domain.OAuthState;
import com.wps.yundoc.credential.domain.WpsOAuthCallbackResult;
import com.wps.yundoc.credential.domain.WpsUserToken;
import com.wps.yundoc.credential.infrastructure.LocalOAuthStateCache;
import com.wps.yundoc.credential.infrastructure.LocalWpsUserTokenCache;
import com.wps.yundoc.wpsclient.application.WpsAuthorizationClient;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
public class WpsUserAuthorizationService {

    private static final long STATE_TTL_SECONDS = 300L;

    private final LocalOAuthStateCache stateCache;
    private final LocalWpsUserTokenCache tokenCache;
    private final WpsAuthorizationClient authorizationClient;
    private final Clock clock;

    public WpsUserAuthorizationService(
            LocalOAuthStateCache stateCache,
            LocalWpsUserTokenCache tokenCache,
            WpsAuthorizationClient authorizationClient) {
        this.stateCache = stateCache;
        this.tokenCache = tokenCache;
        this.authorizationClient = authorizationClient;
        this.clock = Clock.systemUTC();
    }

    public WpsUserToken requireToken(String userId) {
        requireUserId(userId);
        return tokenCache.get(userId).orElseThrow(() -> reauthRequired(userId));
    }

    public WpsOAuthCallbackResult completeAuthorization(String code, String state) {
        OAuthState oauthState = stateCache.consume(state).orElseThrow(this::invalidState);
        requireFresh(oauthState);
        WpsUserToken token = authorizationClient.exchangeCode(code, oauthState.getUserId());
        tokenCache.put(token);
        return new WpsOAuthCallbackResult(oauthState.getUserId());
    }

    private void requireUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new YundocException(YundocErrorCode.USER_ID_REQUIRED);
        }
    }

    private YundocException reauthRequired(String userId) {
        OAuthState state = new OAuthState(newState(), userId, businessSystemId(), expiresAt());
        stateCache.put(state);
        Map<String, Object> details = Collections.singletonMap("authorizeUrl", authorizationClient.authorizeUrl(state));
        return new YundocException(YundocErrorCode.REAUTH_REQUIRED, YundocErrorCode.REAUTH_REQUIRED.getDefaultMessage(), details);
    }

    private String newState() {
        return UUID.randomUUID().toString();
    }

    private String businessSystemId() {
        return RequestContextHolder.current()
                .map(RequestContext::getBusinessSystemId)
                .orElse("unknown");
    }

    private Instant expiresAt() {
        return clock.instant().plusSeconds(STATE_TTL_SECONDS);
    }

    private void requireFresh(OAuthState oauthState) {
        if (oauthState.getExpiresAt().isBefore(clock.instant())) {
            throw invalidState();
        }
    }

    private YundocException invalidState() {
        return new YundocException(YundocErrorCode.VALIDATION_FAILED, "OAuth state is invalid");
    }
}
