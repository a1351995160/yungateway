# B-004 Attack Path

1. A USER flow creates OAuth state for platform user A.
2. Callback is completed with an OAuth code for WPS account B.
3. Gateway stores WPS account B's token under platform user A.
4. Later USER operations for A operate against B's WPS account.

Severity: Medium account-confusion/token-poisoning risk.
