"""Explicit gate for any future external LLM call. No network code lives here."""

from .privacy import assert_redacted


def prepare_redacted_text(manifest: dict, *, consent: bool, reviewed: bool) -> dict:
    if not consent or not reviewed:
        raise PermissionError("External text processing requires consent and privacy review")
    content = [{"evidence_id": item["evidence_id"], "text": item.get("text", "")}
               for item in manifest["observations"] if item.get("text")]
    for item in content:
        assert_redacted(item["text"])
    return {"case_id": manifest["case_id"], "content": content,
            "media_attached": False}
