"""Offline checks for the repaired legacy API. Run: python -m unittest discover -s tests."""

import os
import unittest
from unittest.mock import patch

from app import app
from services.safe_download import UnsafeMediaURL, download_media


class LegacyApiSmokeTest(unittest.TestCase):
    def setUp(self):
        self.client = app.test_client()

    def test_health_and_invalid_requests_do_not_need_cloud_credentials(self):
        with patch.dict(os.environ, {"QWEN_API_KEY": ""}):
            self.assertEqual(self.client.get("/health").status_code, 200)
            self.assertEqual(self.client.post("/diagnose/text", json={}).status_code, 400)
            self.assertEqual(self.client.post("/diagnose/audio", json={}).status_code, 400)

    def test_gaanet_route_does_not_claim_fraud_detection(self):
        response = self.client.post("/diagnose/video_gaanet", json={})
        self.assertEqual(response.status_code, 410)
        self.assertIn("speech separation", response.json["msg"])

    def test_remote_download_is_denied_without_allowlisted_host(self):
        with patch.dict(os.environ, {"JINJING_ALLOWED_MEDIA_HOSTS": ""}):
            with self.assertRaises(UnsafeMediaURL):
                download_media("https://example.com/video.mp4", ".mp4")


if __name__ == "__main__":
    unittest.main()
