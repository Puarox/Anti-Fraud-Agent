"""A scripted planner verifies the graph mechanics; it is not an LLM quality test."""

import tempfile
import unittest
import uuid
from pathlib import Path

from jinjing.agent import Decision, Investigator
from local_web import app


class AdaptivePlanner:
    def decide(self, view):
        observations = view["tool_observations"]
        if not observations:
            return Decision(action="SEARCH_KNOWLEDGE", query="安全账户",
                            hypothesis="对方可能冒充公检法", reason="先找官方提醒")
        if observations[-1]["tool"] == "SEARCH_KNOWLEDGE":
            return Decision(action="CHECK_URL", query="https://www.cac.gov.cn/article",
                            hypothesis="核验资料域名", reason="检索给出官方来源")
        return Decision(action="FINISH", reason="证据已足够给出风险提示")


class InventedUrlPlanner:
    def decide(self, view):
        if not view["tool_observations"]:
            return Decision(action="CHECK_URL", query="https://invented.example/")
        return Decision(action="FINISH")


class FollowupPlanner:
    def decide(self, view):
        if not any("已经转账" in fact["text"] for fact in view["facts"]):
            return Decision(action="ASK_USER", query="是否已经转账？")
        if not view["tool_observations"]:
            return Decision(action="SEARCH_KNOWLEDGE", query="安全账户转账")
        return Decision(action="FINISH")


class ForensicPlanner:
    def decide(self, view):
        observations = view["tool_observations"]
        if not observations:
            return Decision(action="REVIEW_FORENSICS", reason="检查来源和媒体质量")
        if observations[-1]["result"]["audio"]["status"] == "UNKNOWN":
            return Decision(action="ASK_USER", query="能否提供较清晰的原始录音？",
                            reason="现有音频无法判断")
        return Decision(action="FINISH")


class LeakyPlanner:
    def decide(self, view):
        return Decision(action="ASK_USER", query="拨打13800138000确认")


def manifest():
    return {
        "case_id": str(uuid.uuid4()),
        "observations": [{"evidence_id": "ev_001", "text": "让我转账到安全账户"}],
        "risk_indicators": {"level": "CRITICAL", "hits": [{
            "rule_id": "SAFE_ACCOUNT_TRANSFER", "evidence_ids": ["ev_001"]
        }]},
        "url_checks": [{"url": "https://www.cac.gov.cn/article"}],
    }


class AgentGraphTest(unittest.TestCase):
    def test_new_observation_changes_next_tool(self):
        with tempfile.TemporaryDirectory() as directory:
            with Investigator(AdaptivePlanner(), Path(directory) / "state.sqlite") as agent:
                report = agent.run(manifest())
        self.assertEqual(report["status"], "COMPLETED")
        self.assertEqual([x["tool"] for x in report["tool_observations"]],
                         ["SEARCH_KNOWLEDGE", "CHECK_URL"])
        self.assertEqual(len(report["fraud_risk"]["supported_hits"]), 1)

    def test_model_cannot_check_url_it_invented(self):
        with tempfile.TemporaryDirectory() as directory:
            with Investigator(InventedUrlPlanner(), Path(directory) / "state.sqlite") as agent:
                report = agent.run(manifest())
        self.assertEqual(report["tool_observations"][0]["status"], "ERROR")
        self.assertEqual(report["tool_observations"][0]["reason"],
                         "URL was not observed in this case")

    def test_followup_reuses_case_checkpoint_and_new_fact(self):
        case = manifest()
        with tempfile.TemporaryDirectory() as directory:
            with Investigator(FollowupPlanner(), Path(directory) / "state.sqlite") as agent:
                first = agent.run(case)
                self.assertEqual(first["status"], "NEEDS_INPUT")
                case["observations"].append({"evidence_id": "ev_002", "text": "已经转账"})
                second = agent.run(case)
        self.assertEqual(second["status"], "COMPLETED")
        self.assertEqual([x["action"] for x in second["decisions"]],
                         ["ASK_USER", "SEARCH_KNOWLEDGE", "FINISH"])

    def test_followup_keeps_private_placeholders_consistent(self):
        client = app.test_client()
        first = client.post("/api/cases", data={"text": "联系13800138000"}).json
        response = client.post(f"/api/cases/{first['case_id']}/reply", json={
            "text": "13800138000让我再联系13900139000"
        })
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json["observations"][-1]["text"],
                         "[PHONE_1]让我再联系[PHONE_2]")

    def test_forensic_observation_changes_next_action(self):
        case = manifest()
        case["media_type"] = "video"
        case["forensics"] = {"authenticity": "UNKNOWN", "audio": {"status": "UNKNOWN"},
                             "video": {"status": "UNKNOWN"}}
        with tempfile.TemporaryDirectory() as directory:
            with Investigator(ForensicPlanner(), Path(directory) / "state.sqlite") as agent:
                report = agent.run(case)
        self.assertEqual(report["status"], "NEEDS_INPUT")
        self.assertEqual(report["tool_observations"][0]["tool"], "REVIEW_FORENSICS")
        self.assertEqual(report["media_authenticity"]["state"], "UNKNOWN")

    def test_planner_pii_is_blocked_from_report(self):
        with tempfile.TemporaryDirectory() as directory:
            with Investigator(LeakyPlanner(), Path(directory) / "state.sqlite") as agent:
                report = agent.run(manifest())
        self.assertEqual(report["status"], "NEEDS_REVIEW")
        self.assertEqual(report["tool_observations"][0]["status"], "PII_BLOCKED")
        self.assertNotIn("13800138000", str(report))


if __name__ == "__main__":
    unittest.main()
