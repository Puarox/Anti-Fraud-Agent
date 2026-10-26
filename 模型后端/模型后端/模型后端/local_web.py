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
    {"name": "混合知识检索", "kind": "Agent 可调", "group": "Agent 可调工具", "description": "Agent 根据当前疑点组织检索，再将已核对的反诈知识返回规划环。", "availability": "观察后按需选择"},
    {"name": "网址域名核验", "kind": "Agent 可调", "group": "Agent 可调工具", "description": "检查线索中实际出现的网址及官方域名；不访问网页，无信誉源时返回 UNKNOWN。", "availability": "发现网址时"},
    {"name": "媒体取证汇总", "kind": "Agent 可调", "group": "Agent 可调工具", "description": "读取媒体质量、C2PA 来源凭证、AASIST 语音和 DFDS 视频实验信号。", "availability": "案件含媒体时"},
    {"name": "补问与 Checkpoint 恢复", "kind": "Agent 可调", "group": "Agent 可调工具", "description": "缺少关键事实时暂停并追问；用户补充后保留案件状态继续调查。", "availability": "信息不足时"},
    {"name": "本地隐私去敏", "kind": "自动前置", "group": "自动感知与预处理", "description": "手机号、证件号、邮箱等先替换为稳定占位符，原始映射留在本机。", "availability": "文本入库前"},
    {"name": "截图文字 OCR", "kind": "自动前置", "group": "自动感知与预处理", "description": "RapidOCR 从聊天截图及抽样视频帧提取文字、时间戳和画面坐标。", "availability": "图像或视频"},
    {"name": "二维码内容识别", "kind": "自动前置", "group": "自动感知与预处理", "description": "提取截图或视频帧中的二维码文本，并作为待核验线索去敏保存。", "availability": "图像或视频"},
    {"name": "人脸预览遮挡", "kind": "自动前置", "group": "自动感知与预处理", "description": "本机检测预览中的人脸并模糊处理；遮挡结果需要用户检查。", "availability": "图像或视频"},
    {"name": "视频关键帧抽样", "kind": "自动前置", "group": "自动感知与预处理", "description": "按时间间隔抽样视频帧并绑定时间戳，限制时长和帧数。", "availability": "视频证据"},
    {"name": "离线语音转写", "kind": "自动前置", "group": "自动感知与预处理", "description": "使用本地 Whisper 转写音频；没有本地权重时标记不可用，不上传云端。", "availability": "音频或视频"},
    {"name": "诈骗行为规则", "kind": "自动前置", "group": "自动感知与预处理", "description": "匹配安全账户、验证码、屏幕共享、刷单垫资等规则并关联证据编号。", "availability": "案件入库时"},
    {"name": "网址线索提取", "kind": "自动前置", "group": "自动感知与预处理", "description": "从去敏文本、OCR 和语音证据中提取 URL，供 Agent 选择是否核验。", "availability": "有文本证据时"},
    {"name": "媒体技术质量探测", "kind": "自动前置", "group": "多模态取证信号", "description": "ffprobe 读取时长、编码、分辨率和采样率，辅助判断证据是否足够。", "availability": "有媒体文件时"},
    {"name": "C2PA 来源凭证检查", "kind": "自动前置", "group": "多模态取证信号", "description": "离线检查媒体内容凭证及校验状态；无凭证不能据此判定伪造。", "availability": "依赖与格式可用时"},
    {"name": "AASIST 语音伪造信号", "kind": "实验信号", "group": "多模态取证信号", "description": "对音频输出未校准的研究分数，不作为概率或真伪结论。", "availability": "本地权重可用时"},
    {"name": "DFDS 视频人脸信号", "kind": "实验信号", "group": "多模态取证信号", "description": "对抽样人脸帧输出未校准的实验分数；裁剪流程与训练流程存在差异。", "availability": "视频及权重可用时"},
    {"name": "BM25 关键词召回", "kind": "RAG 子模块", "group": "RAG 检索子模块", "description": "对经过时效筛选的资料做中文分词和 BM25 词项匹配。", "availability": "知识检索时"},
    {"name": "BGE 中文向量召回", "kind": "RAG 子模块", "group": "RAG 检索子模块", "description": "本地 BGE-small-zh 语义向量召回；模型缺失时自动退化为 BM25。", "availability": "本地向量权重可用时"},
    {"name": "查询扩展与 RRF 重排", "kind": "RAG 子模块", "group": "RAG 检索子模块", "description": "按反诈词表扩展查询，融合词法与向量排名，并以 RRF 和关键词加权重排。", "availability": "知识检索时"},
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
