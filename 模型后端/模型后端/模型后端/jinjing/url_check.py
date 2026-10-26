"""Offline URL parsing and exact official-domain registry.

No arbitrary website is fetched, and no negative reputation claim is inferred.
"""

from __future__ import annotations

import ipaddress
from urllib.parse import urlsplit


OFFICIAL_DOMAINS = {
    "app.www.gov.cn", "www.cac.gov.cn", "fjca.miit.gov.cn"
}


def check_url(url: str) -> dict:
    parsed = urlsplit(url.strip())
    if parsed.scheme not in {"http", "https"} or not parsed.hostname:
        return {"status": "ERROR", "reason": "invalid_url"}
    host = parsed.hostname.lower().rstrip(".")
    try:
        address = ipaddress.ip_address(host)
    except ValueError:
        address = None
    if address and not address.is_global:
        return {"status": "HIT", "reason": "non_public_ip", "host": host,
                "url": url, "reputation": "UNKNOWN"}
    return {
        "status": "HIT" if host in OFFICIAL_DOMAINS and parsed.scheme == "https" else "UNKNOWN",
        "reason": "curated_official_domain" if host in OFFICIAL_DOMAINS and parsed.scheme == "https" else "no_verified_source",
        "host": host,
        "url": url,
        "reputation": "UNKNOWN",
        "note": "Domain match does not verify this page or its sender; no live risk database was queried.",
    }
