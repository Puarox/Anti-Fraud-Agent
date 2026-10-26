"""Local OCR, QR, frame extraction and optional local Whisper transcription."""

from __future__ import annotations

import os
import subprocess
from pathlib import Path

import cv2
from rapidocr_onnxruntime import RapidOCR

from .privacy import Redactor


_ocr = None
_asr = None


def _ocr_engine():
    global _ocr
    if _ocr is None:
        _ocr = RapidOCR()
    return _ocr


def _asr_engine():
    global _asr
    if _asr is None:
        from faster_whisper import WhisperModel
        default_path = Path(__file__).resolve().parents[4] / "models" / "faster-whisper-base"
        model_path = Path(os.getenv("JINJING_ASR_MODEL_PATH", str(default_path)))
        if not (model_path / "model.bin").exists():
            raise RuntimeError("Local ASR model is not installed; set JINJING_ASR_MODEL_PATH")
        _asr = WhisperModel(str(model_path), device="cpu", compute_type="int8", local_files_only=True)
    return _asr


def inspect_image(image_path: Path, redactor: Redactor, preview_path: Path, at_ms: int = 0) -> dict:
    frame = cv2.imread(str(image_path))
    if frame is None:
        raise ValueError("Image could not be decoded")
    ocr_result, _ = _ocr_engine()(frame)
    observations = []
    preview = frame.copy()
    if ocr_result:
        for polygon, raw_text, score in ocr_result:
            points = [[int(x), int(y)] for x, y in polygon]
            redacted = redactor.redact(raw_text)
            observations.append({
                "type": "ocr", "at_ms": at_ms, "text": redacted,
                "bbox": points, "confidence": round(float(score), 3),
            })
            if redacted != raw_text:
                xs, ys = zip(*points)
                cv2.rectangle(preview, (max(0, min(xs)), max(0, min(ys))),
                              (min(frame.shape[1], max(xs)), min(frame.shape[0], max(ys))),
                              (0, 0, 0), thickness=-1)

    qr_text, qr_points, _ = cv2.QRCodeDetector().detectAndDecode(frame)
    if qr_text:
        observations.append({"type": "qr", "at_ms": at_ms, "text": redactor.redact(qr_text)})
        if qr_points is not None:
            pts = qr_points.astype(int).reshape((-1, 2))
            cv2.fillPoly(preview, [pts], (0, 0, 0))

    cascade = cv2.CascadeClassifier(cv2.data.haarcascades + "haarcascade_frontalface_default.xml")
    faces = cascade.detectMultiScale(cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY),
                                     scaleFactor=1.1, minNeighbors=5, minSize=(32, 32))
    for x, y, width, height in faces:
        preview[y:y + height, x:x + width] = cv2.GaussianBlur(
            preview[y:y + height, x:x + width], (51, 51), 0)

    preview_path.parent.mkdir(parents=True, exist_ok=True)
    if not cv2.imwrite(str(preview_path), preview):
        raise OSError("Could not save redacted preview")
    return {"observations": observations, "preview": preview_path.name,
            "face_redactions": len(faces)}


def video_frames(video_path: Path, frame_dir: Path, interval_s: float = 5.0, max_frames: int = 40) -> list[tuple[Path, int]]:
    capture = cv2.VideoCapture(str(video_path))
    if not capture.isOpened():
        raise ValueError("Video could not be decoded")
    duration_s = capture.get(cv2.CAP_PROP_FRAME_COUNT) / max(capture.get(cv2.CAP_PROP_FPS), 1)
    if duration_s > 600:
        capture.release()
        raise ValueError("Video exceeds the 10-minute local demo limit")
    frame_dir.mkdir(parents=True, exist_ok=True)
    result = []
    for index in range(min(max_frames, max(1, int(duration_s // interval_s) + 1))):
        at_ms = round(index * interval_s * 1000)
        capture.set(cv2.CAP_PROP_POS_MSEC, at_ms)
        ok, frame = capture.read()
        if not ok:
            break
        path = frame_dir / f"frame_{index:03d}.jpg"
        cv2.imwrite(str(path), frame)
        result.append((path, at_ms))
    capture.release()
    return result


def transcribe_local(media_path: Path, wav_path: Path) -> list[dict]:
    command = ["ffmpeg", "-nostdin", "-y", "-i", str(media_path),
               "-vn", "-ac", "1", "-ar", "16000", str(wav_path)]
    completed = subprocess.run(command, capture_output=True, timeout=120, check=False)
    if completed.returncode != 0:
        raise ValueError("Audio could not be extracted or decoded")
    segments, _ = _asr_engine().transcribe(str(wav_path), language="zh", vad_filter=True)
    return [{"type": "asr", "start_ms": round(s.start * 1000),
             "end_ms": round(s.end * 1000), "text": s.text.strip()} for s in segments]
