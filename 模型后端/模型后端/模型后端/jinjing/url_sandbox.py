"""Explicit-consent URL submission adapter; static analysis stays local."""

from __future__ import annotations

import ipaddress
import json
import os
from urllib.parse import parse_qsl, urlsplit
from urllib.request import Request, urlopen

from .privacy import Redactor


def static_preflight(url: str) -> dict:
    try:
        parsed = urlsplit(url.strip())
        host = (parsed.hostname or "").lower().rstrip(".")
        if parsed.scheme not in {"http", "https"} or not host:
            return {"status": "ERROR", "reason": "invalid_url"}
        try:
            ipaddress.ip_address(host)
            is_ip = True
        except ValueError:
            is_ip = False
        labels = host.split(".")
        suspicious = []
        if parsed.username or parsed.password:
            suspicious.append("embedded_credentials")
        if is_ip:
            suspicious.append("ip_literal_host")
        if host.startswith("xn--") or any(label.startswith("xn--") for label in labels):
            suspicious.append("punycode_host")
        if host.count("-") >= 3:
            suspicious.append("many_hyphens_in_host")
        if parsed.port and parsed.port not in {80, 443}:
            suspicious.append("nonstandard_port")
        sensitive_keys = {"token", "code", "phone", "mobile", "account", "id", "session", "auth"}
        query_keys = [key for key, _ in parse_qsl(parsed.query, keep_blank_values=True)]
        sensitive = sorted({key for key in query_keys if key.casefold() in sensitive_keys})
        return {"status": "REVIEW" if suspicious or sensitive else "NO_STATIC_RED_FLAGS",
                "host": host, "scheme": parsed.scheme, "signals": suspicious,
                "sensitive_query_keys": sensitive,
                "note": "静态预检不访问 URL，不检查网页行为或信誉；没有命中不代表安全。"}
    except ValueError:
        return {"status": "ERROR", "reason": "invalid_url"}


def submit_urlscan_private(url: str, explicit_consent: bool) -> dict:
    if not explicit_consent:
        return {"status": "CONSENT_REQUIRED", "provider": "urlscan.io"}
    api_key = os.getenv("URLSCAN_API_KEY", "").strip()
    if not api_key:
        return {"status": "NOT_CONFIGURED", "provider": "urlscan.io",
                "note": "Set URLSCAN_API_KEY before making an explicit private scan request."}
    if Redactor().contains_known_pii(url):
        return {"status": "BLOCKED_PII", "provider": "urlscan.io",
                "note": "URL contains a locally detected identifier; it was not sent."}
    preflight = static_preflight(url)
    if preflight.get("status") == "ERROR":
        return {"status": "ERROR", "reason": preflight.get("reason")}
    payload = json.dumps({"url": url, "visibility": "private"}).encode("utf-8")
    request = Request("https://urlscan.io/api/v1/scan", data=payload, method="POST", headers={
        "Content-Type": "application/json", "API-Key": api_key,
    })
    try:
        with urlopen(request, timeout=15) as response:
            result = json.loads(response.read(256 * 1024).decode("utf-8"))
        return {"status": "SUBMITTED", "provider": "urlscan.io", "visibility": result.get("visibility"),
                "scan_id": result.get("uuid"), "result_url": result.get("result"),
                "note": "URL was sent to urlscan.io. Private visibility limits access but is still external processing."}
    except Exception as exc:
        return {"status": "ERROR", "provider": "urlscan.io", "reason": type(exc).__name__}
