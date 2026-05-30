# Repository Coverage Ledger

| Surface | Evidence | Disposition |
|---|---|---|
| Business JWT issuance/validation | `AuthController`, `AuthTokenService`, `JwtService`, `JwtAuthenticationFilter` | Suppressed: JWT signature, issuer/audience/type/expiry, status, token version, permission version, and API permission checks are present. |
| USER assertion signing/replay | `UserAssertionService`, `LocalUserAssertionNonceCache`, `UserFileController` | Reportable: shared signing key plus no caller-to-user authorization boundary. In-memory nonce cache is acceptable only under documented single-instance/sticky-session deployment. |
| WPS user token cache | `WpsUserAuthorizationService`, `LocalWpsUserTokenCache` | Intentional design: token cache is `userId` only for cross-business reuse. Residual risk is covered by USER assertion authorization boundary finding. |
| APP preview | `AppPreviewRequest`, `AppPreviewService`, `WpsHttpClient` | Reportable: no file ownership/grant check; preview URL/expiry are trusted from WPS and returned directly. |
| OAuth callback | `WpsUserAuthorizationService`, `WpsAuthorizationHttpClient` | Reportable: exchanged WPS token is cached under state user without WPS subject verification. |
| SQL injection | Mapper XML files | Suppressed: `#{}` parameters, static ordering, no `${}` found. |
| XML/XXE/deserialization | `src/main/java` search | Not applicable: no app XML parser/deserialization sinks found. MyBatis DTDs are static framework mapper declarations. |
| Admin auth/RBAC | `AdminAuthService`, `AdminAuthInterceptor`, admin controllers | Suppressed: admin principal and role checks are present for reviewed privileged routes. |
| CSRF/CORS/cookies | `AdminCsrfFilter`, `AdminAuthCookieService`, `AdminConsoleCorsConfiguration` | Suppressed: double-submit token, Origin/Referer allowlist, SameSite=Lax, HttpOnly session cookie, secure-cookie default. |
| Logging/secrets | config and logging searches | Suppressed with residual deployment note: prod secrets are env-backed; local/test placeholders must not be deployed. |
| File/process/path sinks | `src/main/java` search | Not applicable: no upload/archive/path/process execution surface found. |
