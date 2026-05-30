# E:\wps Threat Model

## Assets
- Business-system credentials, internal JWTs, permission versions, and API permission grants.
- Admin session cookies, CSRF token, admin roles, business-system management data.
- WPS app token, WPS user tokens, OAuth states, preview links, and returned WPS file metadata.
- Database records for business systems, permissions, and admin users.

## Trust Boundaries
- External business systems call `/api/v1/auth/token`, `/api/v1/app/**`, and `/api/v1/user/**`.
- Admin console calls `/api/v1/admin/**` with cookie-based auth and CSRF protection.
- The gateway calls WPS upstream APIs using configured app/user credentials.
- MySQL stores credentials, roles, and permission state.

## Attacker-Controlled Inputs
- Public API request bodies, query strings, headers, bearer tokens, USER assertion headers, and OAuth callback parameters.
- Admin login credentials and admin CRUD payloads.
- WPS upstream responses, including preview URLs, token expiry, file metadata, and OAuth token payloads.
- Configuration values in non-production profiles if those profiles are accidentally deployed.

## Security Invariants
- A business system must only receive capabilities allowed by its active permissions and token/permission versions.
- USER requests must bind the asserted user to a trusted caller and must resist tampering and replay.
- WPS tokens and preview links must not leak or outlive the intended authorization boundary.
- Admin operations must require a valid admin principal, role check, CORS allowlist, and CSRF token where cookie auth is used.
- Database access must remain parameterized; XML/deserialization/file/process sinks must not be reachable from attacker input.
