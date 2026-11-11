"""Verify registry-driven dispatch and on-demand tools, not model effectiveness."""

import tempfile
import unittest
import uuid
from pathlib import Path
from unittest.mock import patch

from jinjing.agent import Decision, Investigator
from jinjing.cases import create_case
from jinjing.tool_registry import InvestigationTool


class CustomToolPlanner:
    def __init__(self):
        self.available = set()
        self.first_available = set()

    def decide(self, view):
        self.available = {item["name"] for item in view["available_tools"]}
        if not any(item.get("tool") == "LOOKUP_LOCAL_STATUS"
                   for item in view["tool_observations"]):
            self.first_available = self.available
            return Decision(action="LOOKUP_LOCAL_STATUS", query="case-status",
                            reason="Custom tool is relevant")
        return Decision(action="FINISH", reason="Custom tool returned its observation")


class ToolDispatchTest(unittest.TestCase):
    def test_registered_tool_is_discovered_and_dispatched_without_workflow_edit(self):
        called = []
        tool = InvestigationTool(
            name="LOOKUP_LOCAL_STATUS",
            description="Look up a local status record.",
            input="A local case key.",
            side_effect="read_only",
            available=lambda state: state.get("case_id") == "case-custom",
            execute=lambda state, decision: (
                called.append((state["case_id"], decision["query"])) or {
                    "status": "HIT", "source": "custom_local_fixture",
                    "source_version": "test-v1", "result": {"state": "ready"},
                    "evidence_ids": [], "limitations": []}),
            summarize=lambda observation: ("Custom lookup completed", []),
        )
        planner = CustomToolPlanner()
        manifest = {"case_id": "case-custom", "observations": [],
                    "risk_indicators": {"level": "UNKNOWN", "hits": []},
                    "media_type": "text"}
        with tempfile.TemporaryDirectory() as directory:
            with Investigator(planner, Path(directory) / "state.sqlite",
                              tool_registry={tool.name: tool}) as agent:
                report = agent.run(manifest)
        self.assertIn("LOOKUP_LOCAL_STATUS", planner.first_available)
        self.assertEqual(called, [("case-custom", "case-status")])
        self.assertEqual(report["tool_observations"][0]["source"], "custom_local_fixture")
        self.assertEqual(report["tool_observations"][0]["result"], {"state": "ready"})

    def test_unregistered_action_is_rejected_before_execution(self):
        class InvalidPlanner:
            def decide(self, _view):
                return Decision(action="RUN_ARBITRARY_CODE")

        manifest = {"case_id": "case-invalid", "observations": [],
                    "risk_indicators": {"level": "UNKNOWN", "hits": []},
                    "media_type": "text"}
        with tempfile.TemporaryDirectory() as directory:
            with Investigator(InvalidPlanner(), Path(directory) / "state.sqlite",
                              tool_registry={}) as agent:
                report = agent.run(manifest)
        self.assertEqual(report["status"], "NEEDS_REVIEW")
        self.assertEqual(report["trace"][-1]["status"], "INVALID_ACTION")

    def test_knowledge_and_url_checks_are_not_run_during_case_intake(self):
        with tempfile.TemporaryDirectory() as directory, \
                patch("jinjing.cases.BASE_DIR", Path(directory)), \
                patch("jinjing.retrieval.get_knowledge") as retrieval, \
                patch("jinjing.url_check.check_url") as url_check, \
                patch("jinjing.forensics.inspect_media") as forensics:
            manifest = create_case("请查看 https://sample.example/path")
        retrieval.assert_not_called()
        url_check.assert_not_called()
        forensics.assert_not_called()
        self.assertEqual(manifest["knowledge"]["mode"], "AGENT_ON_DEMAND")
        self.assertEqual(manifest["url_candidates"], ["https://sample.example/path"])
        self.assertEqual(manifest["url_checks"], [])


if __name__ == "__main__":
    unittest.main()
