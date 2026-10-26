"""Offline provenance, media quality and experimental audio anti-spoof signals."""

from __future__ import annotations

import importlib.util
import json
import subprocess
from pathlib import Path

import cv2
import numpy as np


ROOT = Path(__file__).resolve().parents[4]
AASIST_REPO = ROOT / "third_party" / "aasist"
DFDS_WEIGHTS = ROOT / "models" / "dfds-b2" / "pytorch_model.bin"


def inspect_c2pa(path: Path) -> dict:
    try:
        import c2pa
    except ImportError:
        return {"status": "UNAVAILABLE", "reason": "c2pa-python is not installed"}
    try:
        context = c2pa.Context.from_dict({"verify": {
            "remote_manifest_fetch": False, "ocsp_fetch": False,
        }})
        with context, c2pa.Reader(str(path), context=context) as reader:
            report = json.loads(reader.json())
        errors = report.get("validation_status") or []
        return {
            "status": "PRESENT_WITH_ERRORS" if errors else "PRESENT_NO_LOCAL_ERRORS",
            "active_manifest": report.get("active_manifest"),
            "validation_status": errors,
            "meaning": "Offline provenance check only; signer trust and content truth are not established",
        }
    except c2pa.C2paError.ManifestNotFound:
        return {"status": "ABSENT", "meaning": "No credential found; absence does not imply fake media"}
    except c2pa.C2paError.NotSupported:
        return {"status": "UNSUPPORTED", "meaning": "This media format is not supported by the C2PA reader"}
    except (OSError, ValueError, c2pa.C2paError) as exc:
        return {"status": "ERROR", "reason": type(exc).__name__}


def probe_quality(path: Path) -> dict:
    command = ["ffprobe", "-v", "error", "-show_format", "-show_streams", "-of", "json", str(path)]
    try:
        completed = subprocess.run(command, capture_output=True, timeout=20, check=False)
        if completed.returncode != 0:
            return {"status": "ERROR", "reason": "ffprobe_failed"}
        data = json.loads(completed.stdout)
        duration = float(data.get("format", {}).get("duration", 0))
        video = next((s for s in data.get("streams", []) if s.get("codec_type") == "video"), None)
        audio = next((s for s in data.get("streams", []) if s.get("codec_type") == "audio"), None)
        result = {"status": "OK", "duration_s": round(duration, 2),
                  "video": None, "audio": None, "limitations": []}
        if video:
            width, height = int(video.get("width", 0)), int(video.get("height", 0))
            result["video"] = {"width": width, "height": height, "codec": video.get("codec_name")}
            if min(width, height) < 256:
                result["limitations"].append("low_video_resolution")
        if audio:
            result["audio"] = {"sample_rate": int(audio.get("sample_rate", 0)),
                               "codec": audio.get("codec_name")}
            if duration < 2:
                result["limitations"].append("short_audio")
        return result
    except (OSError, ValueError, subprocess.TimeoutExpired):
        return {"status": "ERROR", "reason": "quality_probe_failed"}


class AASISTProbe:
    """Runs the authors' checkpoint. Scores are intentionally not calibrated as probabilities."""

    def __init__(self) -> None:
        import torch
        config = json.loads((AASIST_REPO / "config" / "AASIST.conf").read_text(encoding="utf-8"))
        module_path = AASIST_REPO / "models" / "AASIST.py"
        spec = importlib.util.spec_from_file_location("jinjing_aasist_model", module_path)
        module = importlib.util.module_from_spec(spec)
        assert spec.loader is not None
        spec.loader.exec_module(module)
        self.model = module.Model(config["model_config"])
        weights = AASIST_REPO / "models" / "weights" / "AASIST.pth"
        self.model.load_state_dict(torch.load(weights, map_location="cpu", weights_only=True))
        self.model.eval()
        self.version = "clovaai/aasist ASVspoof2019-LA checkpoint"

    def score(self, path: Path, start_s: float = 0) -> dict:
        import torch
        command = ["ffmpeg", "-nostdin", "-v", "error", "-ss", str(start_s),
                   "-i", str(path), "-t", "4.1", "-vn", "-ac", "1", "-ar", "16000",
                   "-f", "f32le", "pipe:1"]
        completed = subprocess.run(command, capture_output=True, timeout=40, check=False)
        if completed.returncode != 0:
            return {"status": "ERROR", "reason": "audio_decode_failed"}
        samples = np.frombuffer(completed.stdout, dtype="<f4")
        if len(samples) < 32000:
            return {"status": "UNKNOWN", "reason": "less_than_two_seconds_of_audio"}
        if float(np.sqrt(np.mean(samples * samples))) < 0.001:
            return {"status": "UNKNOWN", "reason": "near_silent_audio"}
        size = 64600
        if len(samples) < size:
            samples = np.tile(samples, int(size / len(samples)) + 1)[:size]
        else:
            samples = samples[:size]
        with torch.inference_mode():
            _, logits = self.model(torch.from_numpy(samples.copy()).unsqueeze(0))
        return {
            "status": "UNVALIDATED_SCORE",
            "start_ms": round(start_s * 1000), "end_ms": round((start_s + 4.04) * 1000),
            "bonafide_logit": round(float(logits[0, 1]), 5),
            "model": self.version,
            "meaning": "Research score only; no threshold or probability is validated for phone/social-video audio",
        }


class DFDSProbe:
    """Experimental face crop scores. Our OpenCV crop differs from the model's RetinaFace pipeline."""

    def __init__(self) -> None:
        import torch
        from torchvision import models

        self.torch = torch
        self.model = models.efficientnet_b2(weights=None)
        self.model.classifier = torch.nn.Sequential(torch.nn.Dropout(0.3), torch.nn.Linear(1408, 2))
        checkpoint = torch.load(DFDS_WEIGHTS, map_location="cpu", weights_only=True)
        self.model.load_state_dict(checkpoint["model_state_dict"])
        self.model.eval()
        self.cascade = cv2.CascadeClassifier(cv2.data.haarcascades + "haarcascade_frontalface_default.xml")

    def score_frames(self, frames: list[tuple[Path, int]]) -> dict:
        from PIL import Image
        from torchvision import transforms

        transform = transforms.Compose([
            transforms.Resize((260, 260), interpolation=transforms.InterpolationMode.BICUBIC),
            transforms.ToTensor(),
            transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225]),
        ])
        scores = []
        for path, at_ms in frames[:8]:
            frame = cv2.imread(str(path))
            if frame is None:
                continue
            faces = self.cascade.detectMultiScale(cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY),
                                                  scaleFactor=1.1, minNeighbors=5, minSize=(50, 50))
            if len(faces) == 0:
                continue
            x, y, w, h = max(faces, key=lambda box: box[2] * box[3])
            x1, y1 = max(0, x - 20), max(0, y - 20)
            x2, y2 = min(frame.shape[1], x + w + 20), min(frame.shape[0], y + h + 20)
            crop = Image.fromarray(cv2.cvtColor(frame[y1:y2, x1:x2], cv2.COLOR_BGR2RGB))
            with self.torch.inference_mode():
                logits = self.model(transform(crop).unsqueeze(0))[0]
                score = self.torch.softmax(logits, dim=0)[0].item()
            scores.append({"at_ms": at_ms, "face_bbox": [int(x), int(y), int(w), int(h)],
                           "raw_fake_softmax": round(score, 5)})
        if not scores:
            return {"status": "UNKNOWN", "reason": "no_suitable_frontal_face_in_sampled_frames"}
        return {"status": "UNVALIDATED_SCORE", "frames": scores,
                "model": "ThinothW/DFDS-EfficientNet-B2-Deepfake-Detection-Engine",
                "meaning": "Experimental face signal, not a calibrated probability or authenticity verdict; OpenCV face crop differs from training/inference pipeline"}


_aasist = None
_dfds = None


def inspect_media(path: Path, media_type: str, frames: list[tuple[Path, int]] | None = None) -> dict:
    global _aasist, _dfds
    result = {"authenticity": "UNKNOWN", "provenance": inspect_c2pa(path),
              "quality": probe_quality(path), "audio": {"status": "NOT_APPLICABLE"},
              "video": {"status": "NOT_APPLICABLE"}}
    if media_type in {"audio", "video"}:
        try:
            if _aasist is None:
                _aasist = AASISTProbe()
            result["audio"] = _aasist.score(path)
        except (OSError, RuntimeError, KeyError, ImportError) as exc:
            result["audio"] = {"status": "UNAVAILABLE", "reason": type(exc).__name__}
    if media_type == "video":
        try:
            if not DFDS_WEIGHTS.is_file():
                raise FileNotFoundError("DFDS checkpoint is not installed")
            if _dfds is None:
                _dfds = DFDSProbe()
            result["video"] = _dfds.score_frames(frames or [])
        except (OSError, RuntimeError, KeyError, ImportError) as exc:
            result["video"] = {"status": "UNAVAILABLE", "reason": type(exc).__name__}
    return result
