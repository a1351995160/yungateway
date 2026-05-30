#!/usr/bin/env python3
"""Generate repo-owned SonarCloud external issues.

SonarCloud Quality Profiles are configured outside the repository. This script
keeps high-signal security rules visible in PRs by emitting Sonar's generic
external issue format before the scanner runs.
"""

from __future__ import annotations

import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SOURCE_ROOTS = [ROOT / "src" / "main" / "java"]
OUTPUT = ROOT / "target" / "yundoc-external-issues.json"


RULES = [
    {
        "rule_id": "java-hardcoded-secret",
        "name": "Hardcoded secret-like value",
        "description": "Secret-like values must come from configuration or a secret store.",
        "clean_code_attribute": "TRUSTWORTHY",
        "severity": "CRITICAL",
        "type": "VULNERABILITY",
        "impact_severity": "HIGH",
        "message": "Secret-like value is hardcoded. Read it from configuration or a secret store.",
        "patterns": [
            re.compile(
                r"(?i)(secret|password|api[_-]?key|access[_-]?key|private[_-]?key|clientSecret|signing[_-]?key|pepper)"
                r'[^;\n]{0,80}=\s*"[^"$][^"]{7,}"'
            ),
            re.compile(
                r"(?i)(secret|password|api[_-]?key|access[_-]?key|private[_-]?key|clientSecret|signing[_-]?key|pepper)"
                r'[^;\n]{0,80}\(\s*"[^"$][^"]{7,}"'
            ),
        ],
    },
    {
        "rule_id": "java-weak-crypto",
        "name": "Weak cryptographic primitive",
        "description": "Weak algorithms and modes must not be used for cryptographic operations.",
        "clean_code_attribute": "TRUSTWORTHY",
        "severity": "MAJOR",
        "type": "VULNERABILITY",
        "impact_severity": "MEDIUM",
        "message": "Weak cryptographic primitive or mode detected. Use current approved algorithms.",
        "patterns": [
            re.compile(r'MessageDigest\.getInstance\(\s*"(MD5|SHA-1)"', re.IGNORECASE),
            re.compile(r'Cipher\.getInstance\(\s*"(DES|DESede|RC4|AES/ECB)[^"]*"', re.IGNORECASE),
            re.compile(r'Mac\.getInstance\(\s*"(HmacMD5|HmacSHA1)"', re.IGNORECASE),
        ],
    },
    {
        "rule_id": "java-dynamic-sql",
        "name": "Dynamic SQL construction",
        "description": "SQL must be parameterized instead of built from string interpolation or concatenation.",
        "clean_code_attribute": "TRUSTWORTHY",
        "severity": "CRITICAL",
        "type": "VULNERABILITY",
        "impact_severity": "HIGH",
        "message": "Dynamic SQL construction is risky. Use parameterized MyBatis bindings instead.",
        "patterns": [
            re.compile(r'(?i)@\w*Select\(\s*".*\$\{'),
            re.compile(r'(?i)@\w*Update\(\s*".*\$\{'),
            re.compile(r'(?i)@\w*Delete\(\s*".*\$\{'),
            re.compile(r'(?i)@\w*Insert\(\s*".*\$\{'),
            re.compile(r'(?i)"\s*(select|update|delete|insert)\b[^"]*"\s*\+'),
        ],
    },
    {
        "rule_id": "java-xxe-parser",
        "name": "Unhardened XML parser",
        "description": "XML parser factories must explicitly disable external entities and DTDs.",
        "clean_code_attribute": "TRUSTWORTHY",
        "severity": "CRITICAL",
        "type": "VULNERABILITY",
        "impact_severity": "HIGH",
        "message": "XML parser creation must explicitly disable external entities and DTDs.",
        "patterns": [
            re.compile(r"\bDocumentBuilderFactory\.newInstance\("),
            re.compile(r"\bSAXParserFactory\.newInstance\("),
            re.compile(r"\bXMLInputFactory\.newInstance\("),
        ],
    },
]


def source_files() -> list[Path]:
    files: list[Path] = []
    for root in SOURCE_ROOTS:
        if root.exists():
            files.extend(sorted(root.rglob("*.java")))
    return files


def issue_for(path: Path, line_number: int, line: str, rule: dict[str, object]) -> dict[str, object]:
    relative_path = path.relative_to(ROOT).as_posix()
    start_column = max(1, len(line) - len(line.lstrip()) + 1)
    end_column = max(start_column + 1, len(line.rstrip()) + 1)
    return {
        "ruleId": rule["rule_id"],
        "primaryLocation": {
            "message": rule["message"],
            "filePath": relative_path,
            "textRange": {
                "startLine": line_number,
                "endLine": line_number,
                "startColumn": start_column,
                "endColumn": end_column,
            },
        },
        "effortMinutes": 30,
    }


def main() -> None:
    issues: list[dict[str, object]] = []
    for path in source_files():
        for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), start=1):
            stripped = line.strip()
            if not stripped or stripped.startswith("//") or stripped.startswith("*"):
                continue
            for rule in RULES:
                if any(pattern.search(line) for pattern in rule["patterns"]):
                    issues.append(issue_for(path, line_number, line, rule))

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    rules = [
        {
            "id": rule["rule_id"],
            "name": rule["name"],
            "description": rule["description"],
            "engineId": "yundoc-ci-rules",
            "cleanCodeAttribute": rule["clean_code_attribute"],
            "type": rule["type"],
            "severity": rule["severity"],
            "impacts": [
                {
                    "softwareQuality": "SECURITY",
                    "severity": rule["impact_severity"],
                }
            ],
        }
        for rule in RULES
    ]
    OUTPUT.write_text(json.dumps({"rules": rules, "issues": issues}, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Wrote {len(issues)} Sonar external issue(s) to {OUTPUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
