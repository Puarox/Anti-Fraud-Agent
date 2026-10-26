"""Conservative downloader for the legacy URL-based diagnosis endpoints.

Remote media is disabled until an operator explicitly lists trusted HTTPS hosts.
Local uploads in the v2 API will bypass this downloader.
"""

from __future__ import annotations

import ipaddress
import os
import socket
import tempfile
from urllib.parse import urlsplit

import requests


MAX_BYTES = 25 * 1024 * 1024
TIMEOUT = (5, 30)


class UnsafeMediaURL(ValueError):
    pass


def _allowed_host(url: str) -> str:
    parsed = urlsplit(url)
    if parsed.scheme != "https" or not parsed.hostname or parsed.username or parsed.password:
        raise UnsafeMediaURL("Only HTTPS media URLs without credentials are accepted")
    if parsed.port not in (None, 443):
        raise UnsafeMediaURL("Non-standard media URL ports are disabled")

    hostname = parsed.hostname.lower().rstrip(".")
    allowed = {
        host.strip().lower().rstrip(".")
        for host in os.getenv("JINJING_ALLOWED_MEDIA_HOSTS", "").split(",")
        if host.strip()
    }
    if hostname not in allowed:
        raise UnsafeMediaURL("Media URL host is not on the configured allowlist")

    try:
        addresses = socket.getaddrinfo(hostname, 443, type=socket.SOCK_STREAM)
    except OSError as exc:
        raise UnsafeMediaURL("Media URL host could not be resolved") from exc
    if not addresses or any(
        not ipaddress.ip_address(item[4][0]).is_global for item in addresses
    ):
        raise UnsafeMediaURL("Media URL resolved to a non-public address")
    return hostname


def download_media(url: str, suffix: str) -> str:
    _allowed_host(url)
    path = None
    try:
        with requests.get(
            url, stream=True, allow_redirects=False, timeout=TIMEOUT
        ) as response:
            if 300 <= response.status_code < 400:
                raise UnsafeMediaURL("Media URL redirects are disabled")
            response.raise_for_status()
            declared = response.headers.get("Content-Length")
            if declared and int(declared) > MAX_BYTES:
                raise ValueError("Media file exceeds the size limit")

            total = 0
            with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as target:
                path = target.name
                for chunk in response.iter_content(chunk_size=64 * 1024):
                    total += len(chunk)
                    if total > MAX_BYTES:
                        raise ValueError("Media file exceeds the size limit")
                    target.write(chunk)
        return path
    except Exception:
        if path and os.path.exists(path):
            os.remove(path)
        raise
