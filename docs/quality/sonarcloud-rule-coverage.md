# SonarCloud Rule Coverage

This document maps `docs/coding-standards.md` rules to CI/SonarCloud enforcement.

## Backend CI Path

GitHub Actions runs:

```bash
./mvnw -B verify pmd:pmd
python scripts/sonar_external_issues.py
./mvnw -B sonar:sonar
```

PMD writes `target/pmd.xml`, and SonarCloud imports it through:

```properties
sonar.java.pmd.reportPaths=target/pmd.xml
```

The repository-owned security scanner writes `target/yundoc-external-issues.json`,
and SonarCloud imports it through:

```properties
sonar.externalIssuesReportPaths=target/yundoc-external-issues.json
```

## Automated In This Repository

Alibaba P3C rules are configured in `pom.xml`:

- `rulesets/java/ali-concurrent.xml`
- `rulesets/java/ali-constant.xml`
- `rulesets/java/ali-exception.xml`
- `rulesets/java/ali-flowcontrol.xml`
- `rulesets/java/ali-naming.xml`
- `rulesets/java/ali-oop.xml`
- `rulesets/java/ali-orm.xml`
- `rulesets/java/ali-other.xml`
- `rulesets/java/ali-set.xml`

`rulesets/java/ali-comment.xml` is intentionally not enabled. Its mandatory
`@author` and interface-Javadoc checks create low-value comments and conflict
with the repository rule to avoid empty narration.

Repository-specific PMD rules are configured in `config/pmd/yundoc-java-ruleset.xml`:

| Standard | PMD rule | Threshold |
| --- | --- | --- |
| Method parameters must not exceed 6 | `ExcessiveParameterList` | Reports at 7+ parameters |
| Method length should stay within 40 lines | `ExcessiveMethodLength` | Reports at 41+ lines |
| Class/file length should stay within 250 lines | `ExcessiveClassLength` | Reports at 251+ lines |
| Cyclomatic complexity should stay within 4 | `CyclomaticComplexity` | Reports at 5+ |
| Direct class coupling should stay within 15 | `CouplingBetweenObjects` | Threshold 15 |
| Repeated string literals should be extracted | `AvoidDuplicateLiterals` | Reports above 3 duplicates |
| Do not reassign parameters | `AvoidReassigningParameters` | PMD default |
| Remove unused parameters/imports/locals/private methods | `UnusedFormalParameter`, `UnusedImports`, `UnusedLocalVariable`, `UnusedPrivateMethod` | PMD default |
| Do not assign inside conditions | `AssignmentInOperand` | PMD default |
| Do not return from finally | `ReturnFromFinallyBlock` | PMD default |
| Do not throw/catch broad exception types | `AvoidThrowingRawExceptionTypes`, `AvoidCatchingGenericException` | PMD default |
| Do not hardcode crypto keys or IVs | `HardCodedCryptoKey`, `InsecureCryptoIv` | PMD default |

## Automated External Security Issues

`scripts/sonar_external_issues.py` covers security checks that are normally
Sonar Quality Profile rules but need to be visible from CI without changing
SonarCloud settings:

| Standard | External rule | Notes |
| --- | --- | --- |
| Do not hardcode secrets | `java-hardcoded-secret` | Secret-like assignments and call arguments |
| Do not use weak crypto | `java-weak-crypto` | MD5, SHA-1, DES, RC4, AES/ECB, weak HMAC |
| Do not build dynamic SQL | `java-dynamic-sql` | MyBatis `${}` and string-concatenated SQL |
| Prevent XXE | `java-xxe-parser` | XML parser factories require explicit hardening |

## SonarCloud Quality Profile Recommended

These native Sonar rules should still be enabled or parameterized in the
SonarCloud Java Quality Profile for deeper semantic analysis. The CI rules
above provide repository-owned coverage when the profile is not configured:

- `java:S107` - too many parameters. Set maximum to 6 to match the repository standard.
- `java:S3776` - cognitive complexity.
- `java:S1192` - duplicated string literals.
- `java:S4790` - hardcoded credentials/secrets.
- `java:S4830` - weak cryptography.
- `java:S3649` - SQL injection / dynamic query construction.
- `java:S4036` - file path construction from user input.
- `java:S5144` - unsafe regular expressions.
- `java:S2259` - possible null dereference.

## Manual Or Architecture-Test Coverage

Some standards are intentionally not enforced by PMD because the PMD rule is too strict, too noisy, or not semantically equivalent:

- Maximum 3 returns per method. PMD's `OnlyOneReturn` enforces one return, so it is not enabled.
- Internal class line limits.
- "Complex conditions must be extracted" as a readability rule.
- WPS client layering and "do not bypass the WPS client."
- Domain/module boundaries and transaction placement.
- Security requirements that depend on product intent, such as whether a `fileId` belongs to a caller.

Use ArchUnit, focused tests, or code review for these.
