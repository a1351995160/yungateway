# Security Scan Report - E:\wps

## Executive Summary
The backend has no evidence of SQL injection, XXE/deserialization, user-controlled SSRF, or missing admin CSRF/CORS controls in the reviewed code. The real risks are around authorization boundaries for WPS USER and APP capabilities, especially because the service intentionally reuses WPS user tokens across business systems.

## Findings

### B-001 High - Shared USER assertion key does not prove caller may act as the requested user
Evidence:
- `UserAssertionService` validates `X-Yundoc-User-Id`, timestamp, nonce, and HMAC signature, and signs `businessSystemId + clientId + userId`: `src/main/java/com/wps/yundoc/auth/application/UserAssertionService.java:40`, `:102`, `:111`.
- The signing secret is global: `src/main/resources/application.yml:36`.
- WPS user token lookup is intentionally shared by `userId`: `src/main/java/com/wps/yundoc/credential/application/WpsUserAuthorizationService.java:38`.

Impact: a business system that has the shared signing key and `user-files:list` permission can sign a request for another cached `userId`. That can expose another user's WPS file metadata. This gets more severe when USER write/download routes are implemented.

Recommended fix: keep cross-business WPS token reuse if required, but add a caller-to-user authorization proof. Options include a server-side business-system/user binding table, a trusted user-mapping callback, or a second assertion field that proves the business system owns that user in your platform.

### B-002 Medium/High - APP preview can create preview links for arbitrary caller-supplied WPS file IDs
Evidence:
- `fileId` only has `@NotBlank` and `@Size(max = 128)`: `src/main/java/com/wps/yundoc/capability/apppreview/api/AppPreviewRequest.java:43`.
- `AppPreviewService` validates only `source.type`, then uses the gateway WPS app credential: `src/main/java/com/wps/yundoc/capability/apppreview/application/AppPreviewService.java:25`, `:27`, `:33`.

Impact: any caller with `app-preview:create` can ask the gateway to preview a known file ID. If the WPS app credential has broad visibility, this becomes unauthorized document preview.

Recommended fix: require a file ownership/grant check before calling WPS. If WPS cannot validate caller ownership, require a signed file grant from the owning system or scope WPS credentials by tenant/data domain.

### B-003 Medium - Preview URL and expiry are trusted from WPS and returned directly
Evidence:
- `WpsHttpClient` only checks that `previewUrl` is nonblank and `expireAt` is parseable: `src/main/java/com/wps/yundoc/wpsclient/infrastructure/WpsHttpClient.java:87`, `:99`, `:107`, `:192`.
- The returned direct URL is passed back to the caller unchanged.

Impact: the gateway cannot enforce an expiry after returning a third-party direct URL. If WPS returns a long-lived/non-expiring URL, an unexpected host, or a non-HTTPS URL, current code will relay it.

Recommended fix: validate `https`, allowed host, no userinfo, and `expireAt <= now + requested expireSeconds + small skew`. For strict gateway-controlled expiry, return your own short-lived URL that redirects/proxies to WPS only while a server-side token is valid.

### B-004 Medium - OAuth callback stores WPS token under asserted user without verifying WPS subject
Evidence:
- OAuth state is created for a platform `userId`: `src/main/java/com/wps/yundoc/credential/application/WpsUserAuthorizationService.java:68`.
- Callback exchanges a code and stores the token under `state.getUserId()`: `src/main/java/com/wps/yundoc/credential/application/WpsUserAuthorizationService.java:47`, `:51`, `:52`.
- The token response object contains access token and expiry, not a verified WPS subject: `src/main/java/com/wps/yundoc/wpsclient/infrastructure/AppTokenData.java:5`.

Impact: a valid OAuth code from the wrong WPS account can be bound to the target platform user, causing future USER operations to read the wrong WPS account.

Recommended fix: fetch or verify WPS user identity during callback and compare it to the platform user mapping before caching the token.

## Checked And Not Found
- SQL injection: MyBatis mappers use `#{...}` parameters and static `ORDER BY`; no `${...}` found.
- XML/XXE/deserialization: no application XML parser/deserialization sinks found.
- User-controlled SSRF/open redirect: outbound WPS URLs are config-derived; no redirect sink found.
- Admin CSRF/CORS: unsafe cookie admin requests require CSRF token and allowed Origin/Referer; CORS uses an allowlist.
- Secret leakage/logging: production secrets are env-backed; local/test placeholders are non-prod. No sensitive logging pattern was found.

## Verification
- Frontend `npm audit --registry=https://registry.npmjs.org --json`: 0 vulnerabilities across 615 dependencies.
- Backend tests previously passed on this working tree with MySQL password `123456`: 123 tests, 0 failures, 0 errors.
