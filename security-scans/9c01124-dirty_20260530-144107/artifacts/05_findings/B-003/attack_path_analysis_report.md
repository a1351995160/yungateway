# B-003 Attack Path

1. Caller creates an app preview with requested expiry.
2. WPS returns `previewUrl` and `expireAt`.
3. Gateway checks only nonblank URL and parseable expiry.
4. Caller receives a direct URL that may be longer-lived or outside expected host/scheme.

Severity: Medium. Strict expiry requires a gateway-controlled short-lived redirect/proxy.
