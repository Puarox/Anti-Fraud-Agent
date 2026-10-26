"""Case intake and local-only evidence extraction for the Web workbench."""

from __future__ import annotations

import hashlib
import json
import os
import re
import uuid
from pathlib import Path

from werkzeug.datastructures import FileStorage

from .media import inspect_image, transcribe_local, video_frames
from .forensics import inspect_media
from .privacy import Redactor, assert_redacted


BASE_DIR = Path(os.getenv("JINJING_CASE_DIR", Path(__file__).resolve().parents[1] / "local_cases"))
EXTENSIONS = {
    ".png": "image", ".jpg": "image", ".jpeg": "image",
    ".mp3": "audio", ".wav": "audio", ".m4a": "audio",
    ".mp4": "video", ".mov": "video", ".webm": "video",
}


def _save_upload(upload: FileStorage, path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("wb") as target:
        while chunk := upload.stream.read(64 * 1024):
            digest.update(chunk)
            target.write(chunk)
    return digest.hexdigest()


def create_case(text: str, upload: FileStorage | None = None) -> dict:
    if not text.strip() and (upload is None or not upload.filename):
        raise ValueError("Provide text or one media file")
    case_id = str(uuid.uuid4())
    case_dir = BASE_DIR / case_id
    case_dir.mkdir(parents=True, exist_ok=False)
    redactor = Redactor()
    manifest = {
        "schema_version": "1.0",
        "case_id": case_id,
        "storage": "local_only",
        "text": redactor.redact(text.strip()),
        "media_type": "text",
        "media_sha256": None,
        "observations": [],
        "previews": [],
        "face_redactions": 0,
        "tool_status": {},
        "forensics": {"authenticity": "UNKNOWN", "reason": "no_media"},
        "privacy_notice": "自动去敏可能漏掉人脸、姓名等标识。预览仅保存在这台电脑；媒体不会发送到云端模型。",
    }
    if text.strip():
        manifest["observations"].append({"type": "user_text", "text": manifest["text"]})

    if upload is not None and upload.filename:
        suffix = Path(upload.filename).suffix.lower()
        media_type = EXTENSIONS.get(suffix)
        if not media_type:
            raise ValueError("Unsupported media file extension")
        source = case_dir / f"source{suffix}"
        manifest["media_sha256"] = _save_upload(upload, source)
        manifest["media_type"] = media_type
        frames = []

        if media_type == "image":
            result = inspect_image(source, redactor, case_dir / "preview.jpg")
            manifest["observations"].extend(result["observations"])
            manifest["previews"].append({"at_ms": 0, "file": result["preview"]})
            manifest["face_redactions"] += result["face_redactions"]
            manifest["tool_status"]["ocr_qr"] = "DONE"
        elif media_type == "video":
            frames = video_frames(source, case_dir / "raw_frames")
            for index, (frame_path, at_ms) in enumerate(frames):
                result = inspect_image(frame_path, redactor, case_dir / f"preview_{index:03d}.jpg", at_ms)
                manifest["observations"].extend(result["observations"])
                manifest["previews"].append({"at_ms": at_ms, "file": result["preview"]})
                manifest["face_redactions"] += result["face_redactions"]
            manifest["tool_status"]["ocr_qr"] = "DONE" if frames else "UNKNOWN"

        if media_type in {"audio", "video"}:
            try:
                speech = transcribe_local(source, case_dir / "speech.wav")
                for item in speech:
                    item["text"] = redactor.redact(item["text"])
                manifest["observations"].extend(speech)
                manifest["tool_status"]["asr"] = "DONE"
            except RuntimeError as exc:
                manifest["tool_status"]["asr"] = "UNAVAILABLE"
                manifest["asr_note"] = str(exc)
            except (ValueError, OSError) as exc:
                manifest["tool_status"]["asr"] = "ERROR"
                manifest["asr_note"] = str(exc)

        manifest["forensics"] = inspect_media(source, media_type, frames)
        manifest["tool_status"]["forensics"] = "DONE"

    return _save_case(manifest, redactor, case_dir)


def _save_case(manifest: dict, redactor: Redactor, case_dir: Path) -> dict:
    for index, observation in enumerate(manifest["observations"], start=1):
        observation["evidence_id"] = f"ev_{index:03d}"
        assert_redacted(observation.get("text", ""))
    assert_redacted(manifest["text"])
    from .rules import evaluate_rules
    manifest["risk_indicators"] = evaluate_rules(manifest["observations"])
    combined = " ".join(item.get("text", "") for item in manifest["observations"])
    if combined.strip():
        from .retrieval import get_knowledge
        manifest["knowledge"] = get_knowledge().search(combined[:1000], limit=3)
    else:
        manifest["knowledge"] = {"mode": "NO_QUERY", "results": []}
    from .url_check import check_url
    urls = re.findall(r"https?://[^\s，。；;]+", combined)
    manifest["url_checks"] = [check_url(url) for url in urls[:5]]
    (case_dir / "manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    (case_dir / "private_tokens.json").write_text(
        json.dumps(redactor.private_tokens(), ensure_ascii=False), encoding="utf-8"
    )
    return manifest


def add_user_fact(case_id: str, text: str) -> dict:
    if not text.strip():
        raise ValueError("Additional fact cannot be empty")
    manifest = load_case(case_id)
    case_dir = BASE_DIR / case_id
    token_file = case_dir / "private_tokens.json"
    saved = json.loads(token_file.read_text(encoding="utf-8")) if token_file.exists() else []
    redactor = Redactor(saved)
    manifest["observations"].append({"type": "user_followup", "text": redactor.redact(text.strip())})
    manifest["report_stale"] = True
    return _save_case(manifest, redactor, case_dir)


def load_case(case_id: str) -> dict:
    try:
        parsed = str(uuid.UUID(case_id))
    except ValueError as exc:
        raise FileNotFoundError("Unknown case") from exc
    return json.loads((BASE_DIR / parsed / "manifest.json").read_text(encoding="utf-8"))


def preview_path(case_id: str, filename: str) -> Path:
    load_case(case_id)
    if not filename.startswith("preview") or Path(filename).name != filename:
        raise FileNotFoundError("Unknown preview")
    path = BASE_DIR / case_id / filename
    if not path.is_file():
        raise FileNotFoundError("Unknown preview")
    return path
