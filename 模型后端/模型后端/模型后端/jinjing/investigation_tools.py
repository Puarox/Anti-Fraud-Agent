"""Deterministic, read-only tools for organizing and checking case evidence."""

from __future__ import annotations


def build_evidence_timeline(facts: list[dict]) -> dict:
    """Order evidence by media time when present; otherwise preserve intake order."""
    events = []
    for index, fact in enumerate(facts):
        media_time = fact.get("at_ms", fact.get("start_ms"))
        events.append({
            "evidence_id": fact.get("evidence_id"),
            "type": fact.get("type", "unknown"),
            "at_ms": media_time,
            "text": fact.get("text", ""),
            "intake_order": index + 1,
        })
    events.sort(key=lambda item: (
        item["at_ms"] is None,
        item["at_ms"] if item["at_ms"] is not None else item["intake_order"],
        item["intake_order"],
    ))
    return {
        "events": events,
        "time_basis": "frame_timestamp_or_audio_segment_start_with_intake_order_fallback",
        "limitations": ["Untimestamped text is ordered by intake; this is not proof of real-world event chronology."],
    }


_MATERIAL_FACTS = [
    ("payment_status", ("转账", "汇款", "打款", "付款", "充值", "垫资", "已支付", "没有转"),
     "你是否已经转账、付款或充值？如果有，大约是什么时候、通过什么渠道？"),
    ("credentials_shared", ("验证码", "动态码", "密码", "银行卡", "卡号", "屏幕共享", "远程控制"),
     "对方是否已经拿到验证码、密码、银行卡信息，或控制过你的设备？"),
    ("contact_status", ("仍在联系", "还在通话", "挂断", "停止联系", "拉黑", "删除好友"),
     "你现在还在和对方通话或聊天吗？"),
    ("independent_verification", ("官方客服", "官方渠道", "官方 app", "官方APP", "原平台", "官网核实"),
     "你是否通过自己找到的官方 App、官网或客服电话核实过对方身份？"),
]


def check_evidence_gaps(facts: list[dict], media_type: str) -> dict:
    """Suggest material follow-up questions without treating missing facts as negative."""
    text = "\n".join(str(item.get("text", "")) for item in facts).casefold()
    gaps = [
        {"field": field, "question": question}
        for field, markers, question in _MATERIAL_FACTS
        if not any(marker.casefold() in text for marker in markers)
    ]
    if media_type == "text" and not any(item.get("type") in {"ocr", "audio", "asr", "qr"} for item in facts):
        gaps.append({
            "field": "original_evidence",
            "question": "是否有原始聊天截图、录音或对方发来链接的记录可以补充？",
        })
    return {
        "unknown_fields": [item["field"] for item in gaps],
        "suggested_questions": [item["question"] for item in gaps],
        "meaning": "Missing markers mean unknown, not that the event did not happen.",
        "limitations": ["Keyword checks only; review the case context before asking the user."],
    }


def verify_evidence_chain(facts: list[dict], rules: dict, observations: list[dict],
                          valid_knowledge_ids: set[str]) -> dict:
    """Check whether report evidence references resolve to a case or knowledge record."""
    fact_ids = [item.get("evidence_id") for item in facts if item.get("evidence_id")]
    known_ids = set(fact_ids) | valid_knowledge_ids
    duplicate_ids = sorted({item for item in fact_ids if fact_ids.count(item) > 1})
    references = []
    for hit in rules.get("hits", []):
        references.extend(hit.get("evidence_ids", []))
        source_id = hit.get("source_evidence_id")
        if source_id:
            references.append(source_id)
    for observation in observations:
        references.extend(observation.get("evidence_ids", []))
    unresolved = sorted({item for item in references if item and item not in known_ids})
    return {
        "case_evidence_count": len(fact_ids),
        "reference_count": len(references),
        "duplicate_evidence_ids": duplicate_ids,
        "unresolved_evidence_ids": unresolved,
        "status": "CHECK_REQUIRED" if duplicate_ids or unresolved else "STRUCTURE_OK",
        "meaning": "Checks ID linkage only; it does not validate whether the evidence content is true.",
    }
