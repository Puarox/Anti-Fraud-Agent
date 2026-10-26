"""Conservative local redaction for investigation previews and model egress."""

from __future__ import annotations

import re


PATTERNS = [
    ("ID", re.compile(r"(?<!\d)[1-9]\d{5}(?:18|19|20)\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\d|3[01])\d{3}[0-9Xx](?!\d)")),
    ("PHONE", re.compile(r"(?<!\d)1[3-9]\d{9}(?!\d)")),
    ("EMAIL", re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}")),
    ("ACCOUNT", re.compile(r"(?<!\d)\d{12,19}(?!\d)")),
    ("OTP", re.compile(r"(?:(?:验证码|校验码|动态码|OTP)\s*[:：]?\s*)(\d{4,8})", re.I)),
]


class Redactor:
    def __init__(self, saved_tokens: list[dict] | None = None) -> None:
        self._tokens: dict[tuple[str, str], str] = {
            (item["kind"], item["raw"]): item["token"] for item in (saved_tokens or [])
        }

    def private_tokens(self) -> list[dict]:
        return [{"kind": kind, "raw": raw, "token": token}
                for (kind, raw), token in self._tokens.items()]

    def redact(self, text: str) -> str:
        result = text
        for kind, pattern in PATTERNS:
            def replace(match: re.Match[str]) -> str:
                raw = match.group(1) if kind == "OTP" else match.group(0)
                key = (kind, raw)
                if key not in self._tokens:
                    index = 1 + sum(token_kind == kind for token_kind, _ in self._tokens)
                    self._tokens[key] = f"[{kind}_{index}]"
                if kind == "OTP":
                    return match.group(0).replace(raw, self._tokens[key], 1)
                return self._tokens[key]
            result = pattern.sub(replace, result)
        return result

    def contains_known_pii(self, text: str) -> bool:
        return any(pattern.search(text) for _, pattern in PATTERNS)


def assert_redacted(text: str) -> None:
    if Redactor().contains_known_pii(text):
        raise ValueError("Known PII pattern remains in outbound text")
