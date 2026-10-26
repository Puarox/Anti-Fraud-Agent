"""Evidence grounded warning and source-aware retrieval checks."""

import unittest

from jinjing.egress import prepare_redacted_text
from jinjing.retrieval import LocalKnowledge
from jinjing.rules import evaluate_rules
from jinjing.url_check import check_url
from local_web import app


class RulesAndRetrievalTest(unittest.TestCase):
    def test_critical_rule_cites_user_evidence_and_reviewed_source(self):
        response = app.test_client().post("/api/cases", data={
            "text": "对方自称公检法，让我转账到安全账户"
        })
        self.assertEqual(response.status_code, 201)
        data = response.json
        self.assertEqual(data["risk_indicators"]["level"], "CRITICAL")
        hit = data["risk_indicators"]["hits"][0]
        self.assertEqual(hit["evidence_ids"], ["ev_001"])
        self.assertTrue(any(x["evidence_id"] == hit["source_evidence_id"]
                            for x in LocalKnowledge().search("安全账户")["results"]))

    def test_no_rule_hit_does_not_assert_safety(self):
        result = evaluate_rules([{"evidence_id": "ev_001", "text": "今天天气很好"}])
        self.assertEqual(result["level"], "UNKNOWN")
        self.assertEqual(result["meaning"], "no_rule_hit_is_not_safe")

    def test_exact_official_domain_avoids_lookalike(self):
        self.assertEqual(check_url("https://www.cac.gov.cn/article")["reason"],
                         "curated_official_domain")
        self.assertEqual(check_url("https://www.cac.gov.cn.evil.example/article")["status"],
                         "UNKNOWN")

    def test_egress_requires_review_and_consent(self):
        case = {"case_id": "test", "observations": [
            {"evidence_id": "ev_001", "text": "请联系[PHONE_1]"}
        ]}
        with self.assertRaises(PermissionError):
            prepare_redacted_text(case, consent=False, reviewed=True)
        self.assertFalse(prepare_redacted_text(case, consent=True, reviewed=True)["media_attached"])


if __name__ == "__main__":
    unittest.main()
