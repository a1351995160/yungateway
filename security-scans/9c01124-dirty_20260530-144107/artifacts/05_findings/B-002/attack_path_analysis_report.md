# B-002 Attack Path

1. Caller has `app-preview:create`.
2. Caller submits a known WPS `fileId`.
3. Gateway uses platform WPS app credential.
4. WPS returns a preview link.
5. Gateway relays it to caller.

Severity: Medium/High depending on WPS app-token scope.
