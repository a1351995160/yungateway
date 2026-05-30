# B-001 Attack Path

1. Business system obtains internal JWT.
2. It signs `GET /api/v1/user/files?userId=victim` using the shared USER assertion secret.
3. Gateway validates signature and API permission.
4. Gateway looks up `victim` in the shared WPS user token cache.
5. If cached, WPS file metadata is returned.

Severity: High unless an external user ownership proof is enforced outside this service.
