"""Local privacy and evidence behavior; no cloud requests or model downloads."""

import io
import unittest
from unittest.mock import patch

import cv2
import numpy as np

from local_web import app


class LocalWorkbenchTest(unittest.TestCase):
    def setUp(self):
        self.client = app.test_client()

    def test_text_pii_is_redacted_before_case_response(self):
        response = self.client.post("/api/cases", data={
            "text": "请联系13800138000，验证码123456，收款号6222021234567890123"
        })
        self.assertEqual(response.status_code, 201)
        self.assertNotIn("13800138000", response.get_data(as_text=True))
        self.assertNotIn("6222021234567890123", response.get_data(as_text=True))
        self.assertIn("[PHONE_1]", response.json["text"])
        self.assertIn("[OTP_1]", response.json["text"])

    def test_ocr_locates_and_blacks_out_a_phone_number(self):
        image = np.full((220, 900, 3), 255, np.uint8)
        cv2.putText(image, "CALL 13800138000", (25, 125),
                    cv2.FONT_HERSHEY_SIMPLEX, 2, (0, 0, 0), 4)
        ok, encoded = cv2.imencode(".png", image)
        self.assertTrue(ok)
        response = self.client.post("/api/cases", data={
            "media": (io.BytesIO(encoded.tobytes()), "sample.png")
        }, content_type="multipart/form-data")
        self.assertEqual(response.status_code, 201)
        observations = response.json["observations"]
        self.assertTrue(any(o["text"] == "[PHONE_1]" and o["bbox"] for o in observations))
        preview = self.client.get(f"/cases/{response.json['case_id']}/preview/preview.jpg")
        self.assertEqual(preview.status_code, 200)
        redacted_image = cv2.imdecode(np.frombuffer(preview.data, np.uint8), cv2.IMREAD_COLOR)
        preview.close()
        self.assertLess(float(redacted_image[110, 400].mean()), 50)

    def test_missing_local_asr_is_visible_and_does_not_use_cloud(self):
        with patch("jinjing.cases.transcribe_local", side_effect=RuntimeError("model missing")):
            response = self.client.post("/api/cases", data={
                "media": (io.BytesIO(b"not a real audio file"), "voice.wav")
            }, content_type="multipart/form-data")
        self.assertEqual(response.status_code, 201)
        self.assertEqual(response.json["tool_status"]["asr"], "UNAVAILABLE")


if __name__ == "__main__":
    unittest.main()
