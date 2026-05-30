# Finding Discovery Report

## Scope
- Repository: `E:\wps`
- Branch: `codex/shared-user-assertion-signature`
- Commit: `9c01124` with local uncommitted USER assertion changes
- Review type: static repository security scan plus targeted command evidence

## Promoted Candidates
1. `B-001` - USER assertion accepts a globally shared signing key and has no business-system-to-user authorization boundary.
2. `B-002` - APP preview issues WPS preview links for caller-supplied file IDs with no object ownership/grant check.
3. `B-003` - Preview link response is returned directly without scheme/host validation or gateway-enforced expiry.
4. `B-004` - OAuth callback stores a WPS user token under the asserted platform `userId` without verifying the WPS subject.

## Suppressed Areas
- SQL injection: MyBatis XML mappers use `#{...}` parameters and static `ORDER BY`; no `${...}` or string-built SQL was found.
- XML/XXE/deserialization: no application XML parser/deserializer/object deserialization sinks were found in `src/main/java`.
- SSRF from user input: outbound WPS URLs are configuration-derived, not request-parameter derived.
- Admin CSRF/CORS: unsafe cookie requests require same-origin/allowlisted Origin or Referer plus double-submit CSRF token; CORS is allowlist-only.
- Frontend-only RBAC bypass is not applicable to this backend repo; backend role and capability checks are present on reviewed routes.

## Verification Commands
- `rg` checks over SQL, parser/deserialization, process/file sinks, auth/token, CSRF/CORS, WPS clients, and frontend API surfaces.
- Frontend dependency audit was run in the separate frontend scan and returned zero npm audit vulnerabilities.
- Backend unit tests were previously run on this working tree with `LOCAL_MYSQL_PASSWORD=123456; .\mvnw.cmd test` and passed: 123 tests, 0 failures, 0 errors.
