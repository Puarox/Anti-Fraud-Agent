"""Small action-selection smoke comparison. Hand-written cases are not an accuracy benchmark."""

from __future__ import annotations

import json
import sys
import time
from pathlib import Path

from jinjing.agent import LocalQwenPlanner


CASES = [
    ("safe_account", {"facts": [{"evidence_id": "ev_001", "text": "自称公安要我转账到安全账户"}],
                      "rule_level": "CRITICAL"}, {"SEARCH_KNOWLEDGE"}),
    ("bare_link", {"facts": [{"evidence_id": "ev_001", "text": "对方发来这个网址让我登录"}],
                   "known_urls": ["https://example.net/login"]}, {"CHECK_URL", "ASK_USER"}),
    ("vague_request", {"facts": [{"evidence_id": "ev_001", "text": "帮我看看是不是诈骗"}]}, {"ASK_USER"}),
    ("video_claim", {"facts": [{"evidence_id": "ev_001", "text": "这个视频里的人让我提供验证码"}],
                     "media_type": "video"}, {"REVIEW_FORENSICS", "SEARCH_KNOWLEDGE"}),
    ("audio_unknown", {"facts": [{"evidence_id": "ev_001", "text": "是熟人打来的电话"}],
                       "media_type": "audio", "tool_observations": [{"tool": "REVIEW_FORENSICS",
                           "status": "HIT", "result": {"authenticity": "UNKNOWN",
                           "audio": {"status": "UNKNOWN", "reason": "too_short"}}}],
                       "previous_decisions": [{"action": "REVIEW_FORENSICS", "query": ""}]}, {"ASK_USER"}),
    ("after_search", {"facts": [{"evidence_id": "ev_001", "text": "对方让我转到安全账户"}],
                      "rule_level": "CRITICAL", "tool_observations": [{"tool": "SEARCH_KNOWLEDGE",
                          "status": "HIT", "result": {"results": [{"title": "公安机关反诈提醒",
                          "summary": "公检法不会要求转入安全账户"}]}}],
                      "previous_decisions": [{"action": "SEARCH_KNOWLEDGE", "query": "安全账户"}]},
     {"FINISH", "ASK_USER"}),
]


def main() -> None:
    planner = LocalQwenPlanner(Path(sys.argv[1]) if len(sys.argv) > 1 else None)
    results = []
    for name, partial, acceptable in CASES:
        view = {"facts": [], "rule_level": "UNKNOWN", "media_type": "text",
                "known_urls": [], "tool_observations": [], "previous_decisions": [],
                "remaining_tool_calls": 4} | partial
        start = time.perf_counter()
        try:
            decision = planner.decide(view)
            chosen, reason, error = decision.action, decision.reason, None
        except Exception as exc:
            chosen, reason, error = None, None, type(exc).__name__
        results.append({"case": name, "acceptable": sorted(acceptable),
                        "fixed_first_action": "SEARCH_KNOWLEDGE",
                        "fixed_acceptable": "SEARCH_KNOWLEDGE" in acceptable,
                        "agent_action": chosen, "agent_acceptable": chosen in acceptable,
                        "reason": reason, "error": error,
                        "latency_s": round(time.perf_counter() - start, 2)})
        print(json.dumps(results[-1], ensure_ascii=False), flush=True)
    summary = {"model": str(planner.model_path), "case_count": len(results),
               "fixed_acceptable": sum(x["fixed_acceptable"] for x in results),
               "agent_acceptable": sum(x["agent_acceptable"] for x in results),
               "note": "Exploratory hand-written action smoke test; no population accuracy claim",
               "results": results}
    destination = Path(__file__).with_name(f"comparison_{planner.model_path.name}.json")
    destination.write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Saved {destination}")


if __name__ == "__main__":
    main()
