"""Find similar videos from local case manifests; never upload or open media."""

from __future__ import annotations

import re

from .cases import BASE_DIR


def _terms(text: str) -> set[str]:
    compact = re.sub(r"\s+", "", text.casefold())
    terms = set(re.findall(r"[a-z0-9]{3,}", compact))
    terms.update(compact[index:index + 2] for index in range(max(0, len(compact) - 1)))
    return terms


def find_related_videos(case_id: str, limit: int = 5) -> dict:
    from .cases import load_case

    source = load_case(case_id)
    source_text = " ".join(item.get("text", "") for item in source.get("observations", []))
    source_terms = _terms(source_text)
    matches = []
    scanned = 0
    for manifest_path in BASE_DIR.glob("*/manifest.json"):
        candidate_id = manifest_path.parent.name
        if candidate_id == case_id:
            continue
        try:
            from .cases import load_case as load_candidate
            candidate = load_candidate(candidate_id)
        except (OSError, ValueError):
            continue
        if candidate.get("media_type") != "video":
            continue
        scanned += 1
        candidate_text = " ".join(item.get("text", "") for item in candidate.get("observations", []))
        candidate_terms = _terms(candidate_text)
        union = source_terms | candidate_terms
        lexical = len(source_terms & candidate_terms) / len(union) if union else 0.0
        exact = bool(source.get("media_sha256") and source.get("media_sha256") == candidate.get("media_sha256"))
        if exact or lexical >= 0.06:
            matches.append({"case_id": candidate_id, "media_sha256": candidate.get("media_sha256"),
                            "exact_file_match": exact, "transcript_ocr_overlap": round(lexical, 4),
                            "matched_evidence_ids": [item.get("evidence_id") for item in candidate.get("observations", [])
                                                     if item.get("evidence_id") and
                                                     _terms(item.get("text", "")) & source_terms][:8],
                            "basis": "sha256_exact" if exact else "local_ocr_asr_character_bigram_overlap"})
    matches.sort(key=lambda item: (item["exact_file_match"], item["transcript_ocr_overlap"]), reverse=True)
    return {"candidate_videos_scanned": scanned, "matches": matches[:max(1, min(limit, 20))],
            "scope": "local_case_manifests_only",
            "limitations": ["仅比对本机历史视频的文件哈希及已提取 OCR/ASR 文本；不代表视频相同或内容真实。",
                            "不访问外部视频平台；相似结果必须人工查看原始证据。"]}
