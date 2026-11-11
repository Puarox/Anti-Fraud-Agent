(() => {
  const actionNames = {
    SEARCH_KNOWLEDGE: "检索权威资料",
    CHECK_URL: "核验网址",
    GET_RESPONSE_PLAYBOOK: "生成分场景止损指引",
    PREPARE_REPORTING_CHECKLIST: "准备报警与银行材料",
    FIND_RELATED_VIDEOS: "检索本机相关视频",
    CREATE_REVIEW_TICKET: "建立人工复核工单",
    REVIEW_FORENSICS: "复核媒体取证信号",
    BUILD_EVIDENCE_TIMELINE: "整理证据时间线",
    CHECK_EVIDENCE_GAPS: "检查关键事实缺口",
    VERIFY_EVIDENCE_CHAIN: "校验证据链引用",
    ASK_USER: "向用户补问",
    FINISH: "整理调查结果",
  };

  function el(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined && text !== null && text !== "") node.textContent = text;
    return node;
  }

  function renderTrace(target, events) {
    if (!target) return;
    target.replaceChildren();
    if (!events || !events.length) {
      const empty = el("div", "trace-empty");
      empty.append(el("span", "empty-spark", "✳"), el("p", "", "Agent 正在读取案件状态"),
                   el("small", "", "新观察到达后会重新规划"));
      target.append(empty);
      return;
    }
    events.forEach((event, index) => {
      const kind = event.kind === "plan" ? "plan" : event.kind === "observation" ? "observation" : "error";
      const row = el("article", `trace-event ${kind} trace-queue`);
      const node = el("span", "trace-node", kind === "plan" ? "↗" : kind === "observation" ? "◉" : "!");
      const body = el("div", "trace-body");
      const top = el("div", "trace-topline");
      const title = event.action ? (actionNames[event.action] || event.action) :
                    event.tool ? (actionNames[event.tool] || event.tool) : "规划器需要复核";
      top.append(el("span", "trace-title", title),
                 el("span", "trace-kind", kind === "plan" ? "PLAN" : kind === "observation" ? "OBSERVE" : "REVIEW"));
      if (event.status) top.append(el("span", "trace-kind", event.status));
      body.append(top);
      if (event.reason) body.append(el("p", "", event.reason));
      if (event.hypothesis) body.append(el("p", "", `待核实假设：${event.hypothesis}`));
      if (event.summary) body.append(el("p", "", event.summary));
      if (event.latency_ms !== undefined) {
        const evidence = Array.isArray(event.evidence_ids) && event.evidence_ids.length
          ? ` · 证据 ${event.evidence_ids.join(", ")}` : "";
        body.append(el("p", "trace-telemetry", `耗时 ${event.latency_ms} ms${evidence}`));
      }
      if (event.query) body.append(el("p", "trace-query", event.query));
      if (event.items && event.items.length) {
        const chips = el("div", "trace-chips");
        event.items.forEach((item) => chips.append(el("span", "trace-chip", item)));
        body.append(chips);
      }
      row.append(node, body);
      row.style.animationDelay = `${Math.min(index * 35, 210)}ms`;
      target.append(row);
    });
    target.scrollTop = target.scrollHeight;
  }

  async function pollJob(jobId, onUpdate) {
    while (true) {
      await new Promise((resolve) => window.setTimeout(resolve, 900));
      const response = await fetch(`/api/jobs/${encodeURIComponent(jobId)}`);
      const job = await response.json();
      if (!response.ok) throw new Error(job.error || "无法读取任务状态");
      onUpdate(job);
      if (job.status === "DONE") return job;
      if (job.status === "ERROR") throw new Error(`任务失败：${job.error || "请查看本机日志"}`);
    }
  }

  const fileInput = document.querySelector("#media");
  if (fileInput) fileInput.addEventListener("change", () => {
    const label = document.querySelector("#file-name");
    label.textContent = fileInput.files.length ? fileInput.files[0].name : "未选择文件";
  });

  const intake = document.querySelector("#case-form");
  if (intake) intake.addEventListener("submit", async (event) => {
    event.preventDefault();
    const status = document.querySelector("#job-status");
    const button = intake.querySelector("button[type=submit]");
    button.disabled = true;
    status.textContent = "正在安全地提交本机任务…";
    try {
      const response = await fetch("/api/jobs", { method: "POST", body: new FormData(intake) });
      const job = await response.json();
      if (!response.ok) throw new Error(job.error || "无法创建案件");
      const done = await pollJob(job.job_id, (state) => {
        status.textContent = state.status === "RUNNING" ? "正在提取 OCR、语音和媒体证据…" : "任务正在排队…";
      });
      location.href = `/cases/${done.case_id}`;
    } catch (error) {
      status.textContent = error.message;
      button.disabled = false;
    }
  });

  const demoButton = document.querySelector("#demo-button");
  if (demoButton) demoButton.addEventListener("click", async () => {
    if (demoButton.dataset.caseId) {
      location.href = `/cases/${demoButton.dataset.caseId}`;
      return;
    }
    const state = document.querySelector("#demo-state");
    const trace = document.querySelector("#demo-trace");
    demoButton.disabled = true;
    demoButton.querySelector("span").textContent = "Agent 正在调查…";
    state.textContent = "正在规划";
    state.classList.add("is-running");
    try {
      const response = await fetch("/api/demo", { method: "POST" });
      const task = await response.json();
      if (!response.ok) throw new Error(task.error || "演示无法启动");
      const done = await pollJob(task.job_id, (job) => {
        renderTrace(trace, job.trace || []);
        if (job.trace?.length) state.textContent = job.stage || "正在行动";
      });
      renderTrace(trace, done.trace || []);
      state.textContent = "调查完成";
      state.classList.remove("is-running");
      state.classList.add("is-done");
      demoButton.querySelector("span").textContent = "查看完整案件报告";
      demoButton.dataset.caseId = done.case_id;
      demoButton.disabled = false;
    } catch (error) {
      state.textContent = "需要复核";
      state.classList.remove("is-running");
      trace.replaceChildren(el("div", "trace-empty", error.message));
      demoButton.querySelector("span").textContent = "重试演示";
      demoButton.disabled = false;
    }
  });

  const investigation = document.querySelector("#investigation-form");
  if (investigation) investigation.addEventListener("submit", async (event) => {
    event.preventDefault();
    const status = document.querySelector("#investigation-status");
    const trace = document.querySelector("#agent-trace");
    const button = investigation.querySelector("button[type=submit]");
    button.disabled = true;
    status.textContent = "Agent 正在读取案件证据…";
    try {
      const response = await fetch(investigation.action.replace("/investigate", "/investigation-job"), { method: "POST" });
      const job = await response.json();
      if (!response.ok) throw new Error(job.error || "调查无法启动");
      const done = await pollJob(job.job_id, (state) => {
        renderTrace(trace, state.trace || []);
        status.textContent = state.status === "RUNNING" ? `Agent 正在执行：${state.stage || "观察工具结果"}` : "调查排队中…";
      });
      renderTrace(trace, done.trace || []);
      location.reload();
    } catch (error) {
      status.textContent = error.message;
      button.disabled = false;
    }
  });

  const caseToolsStatus = document.querySelector("#case-tools-status");
  const caseIdMatch = location.pathname.match(/^\/cases\/([0-9a-f-]+)/i);
  const caseId = caseIdMatch?.[1];
  const ticketButton = document.querySelector("#create-review-ticket");
  if (ticketButton && caseId) ticketButton.addEventListener("click", async () => {
    ticketButton.disabled = true;
    try {
      const response = await fetch(`/api/cases/${caseId}/review-ticket`, {
        method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ reason: "用户请求人工复核", priority: "NORMAL" }),
      });
      const result = await response.json();
      if (!response.ok) throw new Error(result.error || "工单建立失败");
      caseToolsStatus.textContent = `工单 ${result.ticket_id} · ${result.status} · ${result.priority}`;
    } catch (error) { caseToolsStatus.textContent = error.message; }
    finally { ticketButton.disabled = false; }
  });

  const videoButton = document.querySelector("#find-related-videos");
  if (videoButton && caseId) videoButton.addEventListener("click", async () => {
    videoButton.disabled = true;
    try {
      const response = await fetch(`/api/cases/${caseId}/related-videos`);
      const result = await response.json();
      if (!response.ok) throw new Error(result.error || "检索失败");
      caseToolsStatus.textContent = result.matches.length
        ? `找到 ${result.matches.length} 条本机候选：${result.matches.map((item) => `${item.case_id.slice(0, 8)} (${item.basis})`).join("；")}`
        : `已扫描 ${result.candidate_videos_scanned} 条本机历史视频，暂未发现相似候选。`;
    } catch (error) { caseToolsStatus.textContent = error.message; }
    finally { videoButton.disabled = false; }
  });

  document.querySelectorAll(".url-sandbox-button").forEach((button) => button.addEventListener("click", async () => {
    if (!caseId) return;
    const url = button.dataset.url;
    const approved = window.confirm(`将完整网址发送给 urlscan.io 做沙箱扫描，并请求 private 可见性。外部服务仍会处理该网址。\n\n${url}\n\n继续？`);
    if (!approved) return;
    button.disabled = true;
    try {
      const response = await fetch(`/api/cases/${caseId}/url-sandbox`, {
        method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ url, confirm_external_submission: true }),
      });
      const result = await response.json();
      if (!response.ok && result.status !== "NOT_CONFIGURED") throw new Error(result.message || result.error || result.status);
      caseToolsStatus.textContent = result.status === "SUBMITTED"
        ? `沙箱任务已提交 (${result.visibility}) · ${result.result_url || result.scan_id}`
        : `沙箱未提交：${result.status} ${result.note || ""}`;
    } catch (error) { caseToolsStatus.textContent = error.message; }
    finally { button.disabled = false; }
  }));

  document.querySelectorAll(".review-ticket [data-status]").forEach((button) => button.addEventListener("click", async () => {
    const card = button.closest(".review-ticket");
    const ticketId = card?.dataset.ticketId;
    if (!ticketId) return;
    const reviewerNote = card.querySelector(".review-note")?.value || "";
    if (button.dataset.status === "RESOLVED" && !reviewerNote.trim()) {
      window.alert("完成复核前请填写复核意见。");
      return;
    }
    button.disabled = true;
    try {
      const response = await fetch(`/api/review-tickets/${ticketId}`, {
        method: "PATCH", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status: button.dataset.status,
          reviewer_note: reviewerNote }),
      });
      const result = await response.json();
      if (!response.ok) throw new Error(result.error || "更新工单失败");
      card.querySelectorAll(".tool-kind")[1].textContent = result.status;
      if (["RESOLVED", "CLOSED"].includes(result.status)) card.remove();
    } catch (error) { window.alert(error.message); }
    finally { button.disabled = false; }
  }));
})();
