"""Forensic evidence must preserve uncertainty even when tools return signals."""

import tempfile
import unittest
import wave
from pathlib import Path

import numpy as np

from jinjing.forensics import inspect_media


class ForensicsTest(unittest.TestCase):
    def test_silent_audio_is_not_called_real_or_fake(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "silent.wav"
            with wave.open(str(path), "wb") as audio:
                audio.setnchannels(1)
                audio.setsampwidth(2)
                audio.setframerate(16000)
                audio.writeframes(np.zeros(16000 * 3, dtype="<i2").tobytes())
            result = inspect_media(path, "audio")
        self.assertEqual(result["authenticity"], "UNKNOWN")
        self.assertEqual(result["audio"]["status"], "UNKNOWN")
        self.assertEqual(result["provenance"]["status"], "ABSENT")


if __name__ == "__main__":
    unittest.main()
