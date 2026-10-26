# 本机工作台 API v1

默认只监听 `127.0.0.1:5050`；数据与媒体均保存在本机 `local_cases/`，没有账号隔离与公网访问设计。

## 异步案件录入

`POST /api/jobs`：`multipart/form-data`，字段 `text`（可空）、`media`（可空，二者至少一个非空）；支持 PNG/JPEG、MP3/WAV/M4A、MP4/MOV/WebM，HTTP 请求体上限 50 MB。返回 HTTP 202 和 `{"job_id":"UUID","status":"QUEUED"}`。

`GET /api/jobs/{job_id}`：返回 `QUEUED`、`RUNNING`、`DONE` 或 `ERROR`。`DONE` 含 `case_id`；`ERROR` 只含异常类型，不回显媒体、文件名或原始文本。单工作进程只并行处理一件录入任务；重启后内存中的 job 状态会消失。

## 案件与调查

- `GET /api/cases/{case_id}`：返回 `schema_version=1.0` 的案件清单。`observations[].evidence_id`、时间戳、画面框和原始媒体 SHA-256 用于回查；文本字段经过本机规则去敏。
- `POST /api/cases/{case_id}/investigate`：运行受控调查，返回 `schema_version=1.0` 的报告；`decisions` 保存计划理由，`tool_observations` 保存工具回执；`fraud_risk` 与 `media_authenticity` 是独立字段。
- `POST /api/cases/{case_id}/investigation-job`：异步提交本机调查，返回 202 和 `job_id`，使用同一个 `/api/jobs/{job_id}` 轮询。浏览器默认使用此入口，避免本地模型运行时卡住页面。
- `POST /api/cases/{case_id}/reply`：JSON `{"text":"补充事实"}`，在本机去敏并追加证据，旧报告标记为过期。
- `GET /api/cases/{case_id}/report`：下载调查 JSON；旧报告过期时返回 HTTP 409。

调查报告可选包含 `metrics`：规划调用数、只读工具调用数、规划/工具累计阶段耗时、工具错误数、Planner 错误数和自动重试数。单次调查的工具观察包含 `contract_version`、工具状态、来源及来源版本、证据编号、观察时间、限制说明、错误码和步骤耗时。耗时只计本地规划/工具阶段，不是端到端 P95；当前自动重试数为 0。

`forensics.authenticity` 当前为 `UNKNOWN`。`audio.bonafide_logit` 和 `video.frames[].raw_fake_softmax` 是实验原始信号，不能转述为真伪概率或诈骗结论。手机接入时，手机上传原件到服务端不属于设备本地处理；需单独设计设备侧去敏与明确授权。当前本机 Web 是手机调试替代入口。
