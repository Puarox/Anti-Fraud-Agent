"""Local Python Web workbench; never binds to a public network interface."""

from __future__ import annotations

import atexit
import json
import threading
import uuid
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

from flask import Flask, abort, jsonify, redirect, render_template_string, request, send_file, url_for
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

STYLE = """
<style>
body {font: 16px/1.55 system-ui, sans-serif; background:#f5f7fb; color:#182536; margin:0}
main {max-width:1000px; margin:36px auto; padding:0 20px}
h1 {font-size:30px; margin-bottom:4px} h2 {font-size:20px}
.sub {color:#536477}.card {background:#fff;border:1px solid #dce4ed;border-radius:14px;padding:20px;margin:16px 0}
textarea {box-sizing:border-box;width:100%;min-height:110px;border:1px solid #bbc8d5;border-radius:8px;padding:10px;font:inherit}
input[type=file] {display:block;margin:12px 0} button {background:#1544a4;color:white;border:0;border-radius:8px;padding:10px 18px;font:inherit;cursor:pointer}
.notice {border-left:4px solid #d8941a;padding:8px 14px;background:#fff7e7}
.evidence {border-top:1px solid #e7edf3;padding:12px 0} .tag {background:#eaf0f8;padding:3px 8px;border-radius:6px;font-size:13px}
.preview {max-width:270px;max-height:220px;object-fit:contain;border:1px solid #dce4ed;margin:7px}
code {word-break:break-all} a {color:#1544a4}
</style>
"""


@app.get("/")
def index():
    return render_template_string(STYLE + """
    <main><h1>金睛鉴诈 · 本地调查工作台</h1>
    <p class="sub">本机调查版：证据提取、风险核验与媒体取证实验信号。当前不作最终诈骗或伪造认定。</p>
    <div class="card"><form id="case-form" method="post" action="/cases" enctype="multipart/form-data">
      <label for="text">补充说明或可疑对话</label><textarea id="text" name="text" placeholder="对方让我做什么？"></textarea>
      <label for="media">截图、音频或视频（可选，最多 50 MB）</label><input id="media" name="media" type="file" accept="image/*,audio/*,video/*">
      <button type="submit">本机提取证据</button>
    </form><p id="job-status" class="sub" role="status"></p></div>
    <p class="notice">本阶段仅在本机处理；自动遮挡可能漏掉姓名、人脸等信息，预览不能直接对外分享。</p></main>
    <script>
    document.getElementById('case-form').addEventListener('submit', async function(event) {
      event.preventDefault();
      const status = document.getElementById('job-status');
      status.textContent = '正在提交本机处理任务…';
      try {
        const response = await fetch('/api/jobs', {method:'POST', body:new FormData(this)});
        const job = await response.json();
        if (!response.ok) throw new Error(job.error || '提交失败');
        while (true) {
          await new Promise(resolve => setTimeout(resolve, 1000));
          const result = await (await fetch('/api/jobs/' + job.job_id)).json();
          if (result.status === 'DONE') { location.href = '/cases/' + result.case_id; return; }
          if (result.status === 'ERROR') throw new Error(result.error || '处理失败');
          status.textContent = result.status === 'RUNNING' ? '正在本机提取证据…' : '任务排队中…';
        }
      } catch (error) { status.textContent = error.message; }
    });
    </script>
    """)


def _run_intake_job(job_id: str, text: str, staged: Path | None, filename: str | None) -> None:
    with _jobs_lock:
        _jobs[job_id]["status"] = "RUNNING"
    try:
        if staged is None:
            result = create_case(text)
        else:
            with staged.open("rb") as source:
                result = create_case(text, FileStorage(stream=source, filename=filename))
        with _jobs_lock:
            _jobs[job_id] = {"status": "DONE", "case_id": result["case_id"]}
    except Exception as exc:
        with _jobs_lock:
            _jobs[job_id] = {"status": "ERROR", "error": type(exc).__name__}
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
        _jobs[job_id] = {"status": "QUEUED"}
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
        return jsonify(status)


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
    return render_template_string(STYLE + """
    <main><a href="/">← 新案件</a><h1>本地证据提取</h1><p class="sub">案件 {{ d.case_id }} · {{ d.media_type }}</p>
    <div class="card"><h2>输入与处理状态</h2><p>{{ d.text or '（无文字补充）' }}</p>
    <p><code>SHA-256: {{ d.media_sha256 or '无媒体' }}</code></p>
    {% for tool,status in d.tool_status.items() %}<span class="tag">{{ tool }}: {{ status }}</span> {% endfor %}
    {% if d.asr_note %}<p>{{ d.asr_note }}</p>{% endif %}</div>
    <div class="card"><h2>时间轴证据</h2>
    {% for o in d.observations %}<div class="evidence"><span class="tag">{{ o.evidence_id }}</span> <span class="tag">{{ o.type }}</span>
    {% if o.at_ms is defined %}{{ o.at_ms }} ms · {% elif o.start_ms is defined %}{{ o.start_ms }} ms · {% endif %}{{ o.text }}
    {% if o.bbox is defined %}<p class="sub">画面坐标：{{ o.bbox }}</p>{% endif %}</div>{% else %}<p>尚无可提取的文本证据。</p>{% endfor %}</div>
    <div class="card"><h2>即时风险信号 · {{ d.risk_indicators.level }}</h2>
    <p class="sub">规则命中仅用于提醒；未命中不代表安全。</p>
    {% for hit in d.risk_indicators.hits %}<div class="evidence"><strong>{{ hit.rule_id }}</strong>
    · {{ hit.matched_text }} · 证据 {{ hit.evidence_ids|join(', ') }}<p>{{ hit.advice }}</p></div>
    {% else %}<p>未命中当前规则，仍需结合具体情境核查。</p>{% endfor %}</div>
    <div class="card"><h2>媒体来源与伪造检测</h2>
    <p class="notice">真实性：{{ d.forensics.authenticity }}。模型原始分数不是伪造概率；缺少 C2PA 凭证不等于伪造。</p>
    <p>来源凭证：{{ d.forensics.provenance.status if d.forensics.provenance is defined else '无媒体' }}</p>
    {% if d.forensics.quality is defined %}<p>质量：{{ d.forensics.quality.status }} · 时长 {{ d.forensics.quality.duration_s or 0 }} 秒 · 限制 {{ d.forensics.quality.limitations|join(', ') }}</p>{% endif %}
    {% if d.forensics.audio is defined %}<p>语音反伪造：{{ d.forensics.audio.status }}{% if d.forensics.audio.bonafide_logit is defined %} · 原始 bona fide logit {{ d.forensics.audio.bonafide_logit }}{% endif %}</p>{% endif %}
    {% if d.forensics.video is defined %}<p>人脸视频检测：{{ d.forensics.video.status }}</p>
    {% for f in d.forensics.video.frames or [] %}<p class="sub">{{ f.at_ms }} ms · 人脸框 {{ f.face_bbox }} · 原始 fake softmax {{ f.raw_fake_softmax }}</p>{% endfor %}{% endif %}
    </div>
    <div class="card"><h2>本地知识检索 · {{ d.knowledge.mode }}</h2>
    {% for k in d.knowledge.results %}<div class="evidence"><a href="{{ k.source_url }}" target="_blank" rel="noopener noreferrer">{{ k.title }}</a>
    <p>{{ k.summary }}</p><p class="sub">{{ k.doc_id }} · 发布 {{ k.published_at }} · 审核 {{ k.reviewed_at }}</p></div>
    {% else %}<p>尚无检索结果。</p>{% endfor %}</div>
    {% if d.url_checks %}<div class="card"><h2>网址初检</h2>
    {% for u in d.url_checks %}<div class="evidence">{{ u.host or '无效网址' }} · {{ u.status }} · {{ u.reason }}<p class="sub">{{ u.note or '' }}</p></div>{% endfor %}
    <p class="sub">尚未接入实时网址信誉库，UNKNOWN 不能解释为安全。</p></div>{% endif %}
    <div class="card"><h2>受控 Agent 调查</h2>
    <form id="investigation-form" method="post" action="{{ url_for('investigate_form',case_id=d.case_id) }}"><button type="submit">运行本地调查</button></form>
    <p id="investigation-status" class="sub" role="status"></p>
    {% if d.report_stale %}<p class="notice">用户已补充新信息，下面是上一轮报告；请重新运行调查。</p>{% endif %}
    {% if report %}<p>状态：{{ report.status }} · 行为风险信号：{{ report.fraud_risk.level }} · 媒体真实性：{{ report.media_authenticity.state }}</p>
    {% if not d.report_stale %}<p><a href="{{ url_for('report_json',case_id=d.case_id) }}">下载可复查 JSON 报告</a></p>{% endif %}
    {% if report.open_question %}<p>待补充：{{ report.open_question }}</p>{% endif %}
    {% for step in report.decisions %}<div class="evidence"><strong>{{ step.action }}</strong> · {{ step.reason }}
    {% if step.hypothesis %}<p>当前假设：{{ step.hypothesis }}</p>{% endif %}
    {% if step.query %}<p>{{ step.query }}</p>{% endif %}</div>{% endfor %}
    {% for step in report.tool_observations %}<div class="evidence">工具 {{ step.tool }} · {{ step.status }}</div>{% endfor %}{% endif %}
    <form method="post" action="{{ url_for('reply_form',case_id=d.case_id) }}">
    <label for="reply">补充新观察</label><textarea id="reply" name="text" placeholder="例如：我已经转账，或对方拒绝通过原号码回拨"></textarea>
    <button type="submit">保存补充事实</button></form></div>
    <div class="card"><h2>本地预览</h2>
    {% for p in d.previews %}<a href="{{ url_for('preview',case_id=d.case_id,filename=p.file) }}"><img class="preview" src="{{ url_for('preview',case_id=d.case_id,filename=p.file) }}" alt="{{ p.at_ms }} ms"></a>{% endfor %}
    <p class="notice">{{ d.privacy_notice }}</p></div></main>
    <script>
    document.getElementById('investigation-form').addEventListener('submit', async function(event) {
      event.preventDefault();
      const status = document.getElementById('investigation-status');
      status.textContent = '调查已提交，本机模型正在核验…';
      try {
        const response = await fetch('/api/cases/{{ d.case_id }}/investigation-job', {method:'POST'});
        const job = await response.json();
        if (!response.ok) throw new Error(job.error || '提交失败');
        while (true) {
          await new Promise(resolve => setTimeout(resolve, 1500));
          const result = await (await fetch('/api/jobs/' + job.job_id)).json();
          if (result.status === 'DONE') { location.reload(); return; }
          if (result.status === 'ERROR') throw new Error(result.error || '调查失败');
          status.textContent = result.status === 'RUNNING' ? '正在核验并记录工具回执…' : '调查排队中…';
        }
      } catch (error) { status.textContent = error.message; }
    });
    </script>
    """, d=data, report=report)


@app.post("/api/cases/<case_id>/investigate")
def investigate_api(case_id):
    try:
        load_case(case_id)
    except (FileNotFoundError, OSError):
        abort(404)
    return jsonify(_perform_investigation(case_id))


def _perform_investigation(case_id: str) -> dict:
    manifest = load_case(case_id)
    with _agent_lock:
        report = _investigator().run(manifest)
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
        _jobs[job_id]["status"] = "RUNNING"
    try:
        _perform_investigation(case_id)
        with _jobs_lock:
            _jobs[job_id] = {"status": "DONE", "case_id": case_id}
    except Exception as exc:
        with _jobs_lock:
            _jobs[job_id] = {"status": "ERROR", "error": type(exc).__name__}


@app.post("/api/cases/<case_id>/investigation-job")
def investigate_job(case_id):
    try:
        load_case(case_id)
    except (FileNotFoundError, OSError):
        abort(404)
    job_id = str(uuid.uuid4())
    with _jobs_lock:
        _jobs[job_id] = {"status": "QUEUED"}
    _job_pool.submit(_run_agent_job, job_id, case_id)
    return jsonify({"job_id": job_id, "status": "QUEUED"}), 202


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
