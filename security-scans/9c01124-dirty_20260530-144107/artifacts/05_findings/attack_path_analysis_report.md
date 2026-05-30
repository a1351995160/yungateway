# Attack Path Analysis

## B-001 USER Assertion Boundary
An external business system obtains its internal JWT, then signs a USER request for an arbitrary `userId` with the globally shared USER assertion secret. The signature binds method/path/query, `businessSystemId`, `clientId`, `userId`, timestamp, and nonce, but it does not prove that the caller is allowed to operate as that user. If a WPS token exists in the shared `userId` cache, `UserFileService` uses it to list files. Severity: High unless an external, enforceable user ownership contract exists outside this service.

## B-002 APP Preview File Grant
A business system with `app-preview:create` submits a known WPS `fileId`. The gateway validates only type and length, obtains the platform WPS app token, asks WPS for a preview link, and returns it. If WPS app credentials can preview that file, there is no local resource ownership check. Severity: Medium/High depending on WPS app-token scope.

## B-003 Preview Link Expiry/Origin
WPS returns `previewUrl` and `expireAt`; the gateway only checks nonblank URL and parseable expiry. A non-HTTPS, unexpected-host, userinfo-bearing, or overlong-lived preview URL would be relayed. Even if the response contains `expireAt`, once a direct WPS URL is returned, this gateway cannot enforce a shorter lifetime. Severity: Medium. Stronger design is an expiring gateway redirect/proxy plus response scheme/host/expiry validation.

## B-004 OAuth Subject Binding
A USER request creates OAuth state for a platform `userId`; callback exchanges any valid WPS code and stores the returned token under that state user. The exchange result currently contains token and expiry only, so the service cannot prove that the WPS account belongs to the asserted platform user. Severity: Medium due to account confusion/token poisoning.
