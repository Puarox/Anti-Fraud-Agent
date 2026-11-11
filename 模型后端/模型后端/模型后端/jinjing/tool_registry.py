"""Extensible registry for tools that the investigation planner may dispatch."""

from __future__ import annotations

from dataclasses import dataclass
from typing import Callable

from .investigation_tools import build_evidence_timeline, check_evidence_gaps, verify_evidence_chain
from .related_media import find_related_videos
from .response_tools import build_response_playbook, prepare_reporting_checklist
from .review_queue import create_ticket
from .url_check import check_url
from .url_sandbox import static_preflight


@dataclass(frozen=True)
class InvestigationTool:
    name: str
    description: str
    input: str
    side_effect: str
    available: Callable[[dict], bool]
    execute: Callable[[dict, dict], dict]
    summarize: Callable[[dict], tuple[str, list[str]]]

    def planner_spec(self) -> dict:
        return {"name": self.name, "description": self.description,
                "input": self.input, "side_effect": self.side_effect}


def _knowledge(state: dict, decision: dict) -> dict:
    from .retrieval import get_knowledge

    query = decision.get("query", "").strip() or " ".join(
        fact.get("text", "") for fact in state.get("facts", []))[:240]
    result = get_knowledge().search(query, limit=3)
    return {"status": "HIT" if result.get("results") else "MISS",
            "source": "knowledge/seed.json", "source_version": "reviewed-seed-v1",
            "result": result,
            "evidence_ids": [hit.get("evidence_id") for hit in result.get("results", [])],
            "limitations": (["vector_model_unavailable_bm25_fallback"]
                            if result.get("mode") == "BM25_ONLY" else [])}


def _check_url(state: dict, decision: dict) -> dict:
    url = decision.get("query", "").strip()
    if url not in state.get("known_urls", []):
        return {"status": "ERROR", "error_code": "UNOBSERVED_URL",
                "reason": "URL was not observed in this case"}
    result = check_url(url)
    result["static_preflight"] = static_preflight(url)
    evidence_ids = [fact.get("evidence_id") for fact in state.get("facts", [])
                    if url in fact.get("text", "")]
    return {"status": result.get("status", "UNKNOWN"),
            "source": "offline_official_domain_registry", "source_version": "curated-domains-v1",
            "result": result, "evidence_ids": evidence_ids,
            "limitations": [result.get("note", "No live reputation source queried")]}


def _forensics(state: dict, _decision: dict) -> dict:
    from .cases import BASE_DIR
    from .forensics import inspect_media

    case_dir = BASE_DIR / state["case_id"]
    source = next(iter(case_dir.glob("source.*")), None)
    if source is not None and source.is_file():
        frames = [(case_dir / "raw_frames" / item["file"], item["at_ms"])
                  for item in state.get("frame_samples", [])
                  if (case_dir / "raw_frames" / item["file"]).is_file()]
        result = inspect_media(source, state.get("media_type", "text"), frames)
    else:
        # Injected manifests used by offline graph callers may already carry forensic observations.
        result = state.get("forensics", {"authenticity": "UNKNOWN", "reason": "media_source_unavailable"})
    return {"status": "HIT", "source": "local_media_forensics",
            "source_version": "local-forensics-v2", "result": result,
            "evidence_ids": [fact.get("evidence_id") for fact in state.get("facts", [])
                             if fact.get("evidence_id")],
            "limitations": ["experimental_scores_are_not_authenticity_verdicts"]}


def _timeline(state: dict, _decision: dict) -> dict:
    result = build_evidence_timeline(state.get("facts", []))
    return {"status": "HIT", "source": "case_evidence_manifest",
            "source_version": "evidence-timeline-v1", "result": result,
            "evidence_ids": [item.get("evidence_id") for item in result["events"]
                             if item.get("evidence_id")], "limitations": result["limitations"]}


def _gaps(state: dict, _decision: dict) -> dict:
    result = check_evidence_gaps(state.get("facts", []), state.get("media_type", "text"))
    return {"status": "HIT", "source": "case_evidence_manifest",
            "source_version": "evidence-gaps-v1", "result": result,
            "evidence_ids": [item.get("evidence_id") for item in state.get("facts", [])
                             if item.get("evidence_id")], "limitations": result["limitations"]}


def _evidence_chain(state: dict, _decision: dict) -> dict:
    from .retrieval import get_knowledge

    valid_ids = {f"kb_{doc['doc_id']}" for doc in get_knowledge().docs}
    result = verify_evidence_chain(state.get("facts", []), state.get("rules", {}),
                                  state.get("observations", []), valid_ids)
    return {"status": result["status"], "source": "case_and_knowledge_manifests",
            "source_version": "evidence-chain-v1", "result": result,
            "evidence_ids": [item.get("evidence_id") for item in state.get("facts", [])
                             if item.get("evidence_id")], "limitations": [result["meaning"]]}


def _playbook(state: dict, _decision: dict) -> dict:
    result = build_response_playbook(state.get("facts", []))
    return {"status": "HIT", "source": "curated_official_response_guidance",
            "source_version": "response-playbook-v1", "result": result,
            "evidence_ids": result["matched_case_evidence_ids"], "limitations": [result["notice"]]}


def _reporting(state: dict, _decision: dict) -> dict:
    checked_urls = [item.get("result", {}) for item in state.get("observations", [])
                    if item.get("tool") == "CHECK_URL" and item.get("result")]
    result = prepare_reporting_checklist(state.get("facts", []), state.get("rules", {}),
                                        checked_urls, state.get("media_sha256"))
    return {"status": "HIT", "source": "case_evidence_manifest",
            "source_version": "reporting-checklist-v1", "result": result,
            "evidence_ids": [item.get("evidence_id") for item in state.get("facts", [])
                             if item.get("evidence_id")], "limitations": [result["notice"]]}


def _related_videos(state: dict, _decision: dict) -> dict:
    result = find_related_videos(state["case_id"])
    return {"status": "HIT" if result["matches"] else "MISS",
            "source": "local_case_manifests", "source_version": "related-video-v1",
            "result": result,
            "evidence_ids": [evidence_id for match in result["matches"]
                             for evidence_id in match["matched_evidence_ids"]],
            "limitations": result["limitations"]}


def _review_ticket(state: dict, decision: dict) -> dict:
    priority = "URGENT" if state.get("rules", {}).get("level") == "CRITICAL" else "HIGH"
    evidence_ids = [fact.get("evidence_id") for fact in state.get("facts", []) if fact.get("evidence_id")]
    hit_reasons = [hit.get("rule_id", "") for hit in state.get("rules", {}).get("hits", [])]
    media_reason = state.get("forensics", {}).get("reason", "")
    reason = decision.get("query", "").strip() or (
        "风险规则待人工复核：" + "、".join(hit_reasons) if hit_reasons
        else f"媒体线索需要复核：{media_reason or '真实性尚未确认'}")
    result = create_ticket(state["case_id"], reason, evidence_ids, priority)
    return {"status": "HIT", "source": "local_review_queue",
            "source_version": "review-ticket-v1", "result": result,
            "evidence_ids": result["evidence_ids"],
            "limitations": ["工单已建立不代表人工已经受理或完成复核。"]}


def _summary(text: str, items: Callable[[dict], list[str]] | None = None):
    return lambda result: (text, items(result) if items else [])


def default_tool_registry() -> dict[str, InvestigationTool]:
    """Return built-in tools; callers may pass additional tools to Investigator."""
    return {
        "SEARCH_KNOWLEDGE": InvestigationTool(
            "SEARCH_KNOWLEDGE", "Search reviewed anti-fraud guidance using hybrid keyword and vector retrieval.",
            "A short question grounded in observed case facts.", "read_only",
            lambda state: bool(state.get("facts")), _knowledge,
            _summary("检索到知识资料", lambda result: [item.get("title", "") for item in result.get("result", {}).get("results", [])[:3]])),
        "CHECK_URL": InvestigationTool(
            "CHECK_URL", "Check an observed URL against an offline registry and local static-risk heuristics.",
            "An exact URL copied from case evidence.", "read_only",
            lambda state: bool(state.get("known_urls")), _check_url,
            lambda result: (f"网址状态：{result.get('result', {}).get('status', result.get('status', 'UNKNOWN'))}；"
                            f"本地预检：{result.get('result', {}).get('static_preflight', {}).get('status', 'UNKNOWN')}", [])),
        "REVIEW_FORENSICS": InvestigationTool(
            "REVIEW_FORENSICS", "On demand, inspect local media provenance, technical quality and available experimental signals.",
            "The case's local original media and sampled frames.", "local_compute",
            lambda state: state.get("media_type") not in {"text", None}, _forensics,
            lambda result: ("复核了本地媒体取证信号；分数不是真伪结论", [])),
        "BUILD_EVIDENCE_TIMELINE": InvestigationTool(
            "BUILD_EVIDENCE_TIMELINE", "Arrange observations by media timestamp or intake order.",
            "Current case observations and evidence IDs.", "read_only",
            lambda state: len(state.get("facts", [])) > 1, _timeline,
            lambda result: (f"整理 {len(result.get('result', {}).get('events', []))} 条证据线索", [])),
        "CHECK_EVIDENCE_GAPS": InvestigationTool(
            "CHECK_EVIDENCE_GAPS", "Find material facts not yet mentioned and suggest follow-up questions.",
            "Redacted case observations.", "read_only",
            lambda state: bool(state.get("facts")) or state.get("media_type") not in {"text", None}, _gaps,
            _summary("检查待确认事实", lambda result: result.get("result", {}).get("suggested_questions", [])[:3])),
        "VERIFY_EVIDENCE_CHAIN": InvestigationTool(
            "VERIFY_EVIDENCE_CHAIN", "Check evidence references against case and knowledge records.",
            "Current evidence, rules and tool observations.", "read_only",
            lambda state: bool(state.get("facts")), _evidence_chain,
            lambda result: (f"证据引用检查：{result.get('result', {}).get('status', 'UNKNOWN')}", [])),
        "GET_RESPONSE_PLAYBOOK": InvestigationTool(
            "GET_RESPONSE_PLAYBOOK", "Return conditional, source-backed next steps for common scam situations.",
            "Current redacted case signals.", "read_only",
            lambda state: bool(state.get("facts")), _playbook,
            _summary("生成分场景止损建议；请确认条件是否适用",
                     lambda result: [item.get("scenario", "") for item in result.get("result", {}).get("branches", [])])),
        "PREPARE_REPORTING_CHECKLIST": InvestigationTool(
            "PREPARE_REPORTING_CHECKLIST", "Organize case evidence and missing details for contacting bank or police.",
            "Case evidence, rule hits, observed URL checks and media hash.", "read_only",
            lambda state: bool(state.get("facts")), _reporting,
            _summary("整理了联系银行/报警前的材料清单",
                     lambda result: result.get("result", {}).get("details_to_prepare", [])[:3])),
        "FIND_RELATED_VIDEOS": InvestigationTool(
            "FIND_RELATED_VIDEOS", "Search local historical videos by exact hash and extracted OCR/ASR overlap.",
            "Current case id and local case manifests.", "local_read_only",
            lambda state: state.get("media_type") == "video", _related_videos,
            _summary("检索本机历史视频候选",
                     lambda result: [f"{item['case_id'][:8]} · {item['basis']}"
                                     for item in result.get("result", {}).get("matches", [])[:3]])),
        "CREATE_REVIEW_TICKET": InvestigationTool(
            "CREATE_REVIEW_TICKET", "Create or reuse a local human-review ticket linked to case evidence.",
            "A concise reason; ticket creation is a local write and does not mean review is complete.", "local_write",
            lambda state: (state.get("rules", {}).get("level") in {"HIGH", "CRITICAL"}
                           or (state.get("media_type") not in {"text", None}
                               and state.get("forensics", {}).get("authenticity") in {"UNKNOWN", "NEEDS_REVIEW"})),
            _review_ticket,
            lambda result: (f"人工复核工单 {result.get('result', {}).get('status')} · {result.get('result', {}).get('priority')}",
                            [result.get("result", {}).get("ticket_id", "")])),
    }
