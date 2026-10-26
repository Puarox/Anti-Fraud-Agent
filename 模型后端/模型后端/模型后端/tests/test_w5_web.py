"""The browser job flow must return a completed, redacted case."""

import time
import unittest
from unittest.mock import patch

from local_web import app


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
            worker.assert_called_once_with(case["case_id"])


if __name__ == "__main__":
    unittest.main()
