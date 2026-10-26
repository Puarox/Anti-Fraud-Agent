"""The browser job flow must return a completed, redacted case."""

import json
import time
import unittest
from unittest.mock import patch

from local_web import BASE_DIR, app


class AsyncWorkbenchTest(unittest.TestCase):
    def test_async_intake_and_evidence_view(self):
        client = app.test_client()
        submitted = client.post("/api/jobs", data={"text": "13800138000让我转账到安全账户"})
        self.assertEqual(submitted.status_code, 202)
        job_id = submitted.json["job_id"]
        deadline = time.monotonic() + 40
        while time.monotonic() < deadline:
            status = client.get(f"/api/jobs/{job_id}").json
            if status["status"] == "DONE":
                break
            if status["status"] == "ERROR":
                self.fail(status["error"])
            time.sleep(0.2)
        else:
            self.fail("Intake job timed out")
        case = client.get(f"/api/cases/{status['case_id']}").json
        self.assertIn("[PHONE_1]", case["text"])
        self.assertNotIn("13800138000", str(case))
        page = client.get(f"/cases/{status['case_id']}")
        self.assertIn(b"ev_001", page.data)

    def test_async_investigation_job_reports_completion(self):
        client = app.test_client()
        case = client.post("/api/cases", data={"text": "有人让我转账"}).json
        with patch("local_web._perform_investigation", return_value={"status": "COMPLETED"}) as worker:
            submitted = client.post(f"/api/cases/{case['case_id']}/investigation-job")
            self.assertEqual(submitted.status_code, 202)
            deadline = time.monotonic() + 15
            while time.monotonic() < deadline:
                status = client.get(f"/api/jobs/{submitted.json['job_id']}").json
                if status["status"] == "DONE":
                    break
                time.sleep(0.05)
            else:
                self.fail("Investigation job timed out")
            worker.assert_called_once()
            self.assertEqual(worker.call_args.args[0], case["case_id"])
            self.assertEqual(worker.call_args.args[1], submitted.json["job_id"])

    def test_case_page_renders_agent_trace_with_optional_items(self):
        client = app.test_client()
        case = client.post("/api/cases", data={"text": "合成案件线索"}).json
        report = {
            "status": "COMPLETED",
            "planner_model": "test-planner",
            "fraud_risk": {"level": "HIGH"},
            "media_authenticity": {"state": "UNKNOWN"},
            "open_question": None,
            "budget_used": 2,
            "metrics": {"planner_calls": 3, "tool_calls": 2,
                        "planner_latency_ms": 5, "tool_latency_ms": 1},
            "trace": [
                {"kind": "plan", "action": "SEARCH_KNOWLEDGE", "query": "客服退款"},
                {"kind": "observation", "tool": "SEARCH_KNOWLEDGE", "status": "HIT",
                 "summary": "检索到 1 条资料", "items": ["官方反诈指南"]},
            ],
        }
        report_path = BASE_DIR / case["case_id"] / "investigation.json"
        report_path.write_text(json.dumps(report, ensure_ascii=False), encoding="utf-8")
        page = client.get(f"/cases/{case['case_id']}")
        self.assertEqual(page.status_code, 200)
        self.assertIn("SEARCH_KNOWLEDGE".encode(), page.data)
        self.assertIn("官方反诈指南".encode(), page.data)
        self.assertIn("3 / 2".encode(), page.data)
        self.assertIn("5 / 1 ms".encode(), page.data)


if __name__ == "__main__":
    unittest.main()
