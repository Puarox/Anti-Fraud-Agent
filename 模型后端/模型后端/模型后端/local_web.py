"""Local Python Web workbench; never binds to a public network interface."""

from __future__ import annotations

import atexit
import json
import threading
import uuid
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

from flask import Flask, abort, jsonify, redirect, render_template, request, send_file, url_for
from werkzeug.datastructures import FileStorage

from jinjing.agent import Investigator, LocalQwenPlanner
from jinjing.cases import BASE_DIR, add_user_fact, create_case, load_case, preview_path


app = Flask(__name__)
app.config["MAX_CONTENT_LENGTH"] = 50 * 1024 * 1024
_agent = None
_agent_lock = threading.Lock()
_job_pool = ThreadPoolExecutor(max_workers=1, thread_name_prefix="jinjing-intake")
_jobs: dict[str, dict] = {}
_jobs_lock = threading.Lock()

TOOL_CATALOG = [
    {"name": "本地知识检索", "kind": "RAG", "description": "BM25 + 中文向量召回 + RRF 融合，返回已核对的官方反诈资料。", "availability": "调查时可调用"},
    {"name": "网址初检", "kind": "核验", "description": "识别精确官方域名与仿冒域名；无实时信誉源时明确返回 UNKNOWN。", "availability": "发现网址时"},
    {"name": "媒体取证复核", "kind": "多模态", "description": "查看 C2PA 来源、媒体质量、AASIST 语音信号和视频人脸实验信号。", "availability": "有音视频时"},
    {"name": "用户补充事实", "kind": "交互", "description": "信息不足时暂停调查，追问关键事实后从 Checkpoint 恢复。", "availability": "按需触发"},
    {"name": "规则风险提示", "kind": "规则", "description": "识别安全账户、验证码、屏幕共享和刷单垫资等高风险行为。", "availability": "录入后自动运行"},
    {"name": "本地隐私去敏", "kind": "隐私", "description": "手机号、证件号、邮箱等先替换为稳定占位符，再进入检索与规划。", "availability": "录入时自动运行"},
]


def _investigator() -> Investigator:
    global _agent
    if _agent is None:
        _agent = Investigator(LocalQwenPlanner())
    return _agent


@atexit.register
def _close_agent():
    if _agent is not None:
        _agent.close()
    _job_pool.shutdown(wait=False, cancel_futures=True)

@app.get("/")
def index():
    return render_template("index.html", tools=TOOL_CATALOG)


def _run_intake_job(job_id: str, text: str, staged: Path | None, filename: str | None) -> None:
    with _jobs_lock:
        _jobs[job_id].update({"status": "RUNNING", "stage": "提取证据"})
    try:
        if staged is None:
            result = create_case(text)
        else:
            with staged.open("rb") as source:
                result = create_case(text, FileStorage(stream=source, filename=filename))
        with _jobs_lock:
            _jobs[job_id].update({"status": "DONE", "stage": "完成", "case_id": result["case_id"]})
    except Exception as exc:
        app.logger.exception("Local intake job failed")
        with _jobs_lock:
            _jobs[job_id].update({"status": "ERROR", "error": type(exc).__name__})
    finally:
        if staged is not None:
            staged.unlink(missing_ok=True)


@app.post("/api/jobs")
def submit_job():
    text_value = request.form.get("text", "")
    upload = request.files.get("media")
    if not text_value.strip() and (upload is None or not upload.filename):
        return jsonify({"error": "Provide text or one media file"}), 400
    staged = None
    filename = upload.filename if upload is not None and upload.filename else None
    if filename:
        suffix = Path(filename).suffix.lower()
        from jinjing.cases import EXTENSIONS
        if suffix not in EXTENSIONS:
            return jsonify({"error": "Unsupported media file extension"}), 400
        staging_dir = BASE_DIR / "_jobs"
        staging_dir.mkdir(parents=True, exist_ok=True)
        staged = staging_dir / f"{uuid.uuid4()}{suffix}"
        upload.save(staged)
    job_id = str(uuid.uuid4())
    with _jobs_lock:
        _jobs[job_id] = {"status": "QUEUED", "trace": [], "task": "intake"}
    _job_pool.submit(_run_intake_job, job_id, text_value, staged, filename)
    return jsonify({"job_id": job_id, "status": "QUEUED"}), 202


@app.get("/api/jobs/<job_id>")
def job_status(job_id):
    try:
        job_id = str(uuid.UUID(job_id))
    except ValueError:
        abort(404)
    with _jobs_lock:
        status = _jobs.get(job_id)
        if status is None:
            abort(404)
        return jsonify(dict(status))


@app.post("/cases")
def create_from_form():
    try:
        result = create_case(request.form.get("text", ""), request.files.get("media"))
    except ValueError as exc:
        return jsonify({"error": str(exc)}), 400
    return redirect(url_for("show_case", case_id=result["case_id"]))


@app.post("/api/cases")
def create_from_api():
    try:
        result = create_case(request.form.get("text", ""), request.files.get("media"))
    except ValueError as exc:
        return jsonify({"error": str(exc)}), 400
    return jsonify(result), 201


@app.get("/api/cases/<case_id>")
def case_json(case_id):
    try:
        return jsonify(load_case(case_id))
    except (FileNotFoundError, OSError):
        abort(404)


@app.get("/cases/<case_id>")
def show_case(case_id):
    try:
        data = load_case(case_id)
    except (FileNotFoundError, OSError):
        abort(404)
    report_file = BASE_DIR / case_id / "investigation.json"
    report = json.loads(report_file.read_text(encoding="utf-8")) if report_file.exists() else None
    return render_template("case.html", d=data, report=report, tools=TOOL_CATALOG)


@app.post("/api/cases/<case_id>/investigate")
def investigate_api(case_id):
    try:
        load_case(case_id)
    except (FileNotFoundError, OSError):
        abort(404)
    return jsonify(_perform_investigation(case_id))


def _perform_investigation(case_id: str, job_id: str | None = None) -> dict:
    manifest = load_case(case_id)
    with _agent_lock:
        agent = _investigator()
        if job_id:
            def publish(event: dict) -> None:
                with _jobs_lock:
                    if job_id in _jobs:
                        _jobs[job_id]["trace"].append(event)
                        _jobs[job_id]["stage"] = event.get("action") or event.get("tool") or "观察"
            agent.progress_callback = publish
        try:
            report = agent.run(manifest)
        finally:
            agent.progress_callback = None
    (BASE_DIR / case_id / "investigation.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    manifest["report_stale"] = False
    (BASE_DIR / case_id / "manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    return report


def _run_agent_job(job_id: str, case_id: str) -> None:
    with _jobs_lock:
        _jobs[job_id].update({"status": "RUNNING", "stage": "Agent 正在规划"})
    try:
        _perform_investigation(case_id, job_id)
        with _jobs_lock:
            _jobs[job_id].update({"status": "DONE", "stage": "调查完成", "case_id": case_id})
    except Exception as exc:
        app.logger.exception("Local Agent job failed")
        with _jobs_lock:
            _jobs[job_id].update({"status": "ERROR", "error": type(exc).__name__})


@app.post("/api/cases/<case_id>/investigation-job")
def investigate_job(case_id):
    try:
        load_case(case_id)
    except (FileNotFoundError, OSError):
        abort(404)
    job_id = str(uuid.uuid4())
    with _jobs_lock:
        _jobs[job_id] = {"status": "QUEUED", "trace": [], "task": "investigation"}
    _job_pool.submit(_run_agent_job, job_id, case_id)
    return jsonify({"job_id": job_id, "status": "QUEUED"}), 202


@app.post("/api/demo")
def start_demo():
    demo_text = (
        "合成演示案例：有人自称平台客服，说账户异常要退款，要求安装远程会议软件、开启屏幕共享并提供短信验证码。"
        "对方发来网址 https://account-check.example/verify。"
    )
    case = create_case(demo_text)
    job_id = str(uuid.uuid4())
    with _jobs_lock:
        _jobs[job_id] = {"status": "QUEUED", "trace": [], "task": "demo", "case_id": case["case_id"]}
    _job_pool.submit(_run_agent_job, job_id, case["case_id"])
    return jsonify({"job_id": job_id, "case_id": case["case_id"], "status": "QUEUED"}), 202


@app.get("/api/cases/<case_id>/report")
def report_json(case_id):
    try:
        manifest = load_case(case_id)
        if manifest.get("report_stale"):
            return jsonify({"error": "Report is stale; rerun investigation"}), 409
        report = BASE_DIR / case_id / "investigation.json"
        return send_file(report, mimetype="application/json", as_attachment=True,
                         download_name=f"jinjing-{case_id}.json")
    except (FileNotFoundError, OSError):
        abort(404)


@app.post("/cases/<case_id>/investigate")
def investigate_form(case_id):
    response = investigate_api(case_id)
    if response.status_code != 200:
        return response
    return redirect(url_for("show_case", case_id=case_id))


@app.post("/api/cases/<case_id>/reply")
def reply_api(case_id):
    data = request.get_json(silent=True) or {}
    try:
        return jsonify(add_user_fact(case_id, data.get("text", "")))
    except (FileNotFoundError, OSError):
        abort(404)
    except ValueError as exc:
        return jsonify({"error": str(exc)}), 400


@app.post("/cases/<case_id>/reply")
def reply_form(case_id):
    try:
        add_user_fact(case_id, request.form.get("text", ""))
    except (FileNotFoundError, OSError):
        abort(404)
    except ValueError as exc:
        return jsonify({"error": str(exc)}), 400
    return redirect(url_for("show_case", case_id=case_id))


@app.get("/cases/<case_id>/preview/<filename>")
def preview(case_id, filename):
    try:
        return send_file(preview_path(case_id, filename), mimetype="image/jpeg")
    except (FileNotFoundError, OSError):
        abort(404)


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5050, debug=False)
