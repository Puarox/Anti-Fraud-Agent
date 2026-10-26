# 金睛鉴诈改造工作区

本目录从原始 RAR 的源码复制而来。原始压缩包与只读检查目录未修改。

## 当前入口

模型后端目录：`模型后端/模型后端/模型后端`。从该目录执行：

```powershell
python app.py
python -m unittest discover -s tests -v
```

本机 Web 工作台从同一目录运行 `python local_web.py`，浏览器打开 `http://127.0.0.1:5050`。只需本地能力时可用 `requirements-local.txt` 建独立环境。OCR 模型由 `rapidocr-onnxruntime` 随包提供；离线 ASR 默认读取工作区 `models/faster-whisper-base`，也可设置 `JINJING_ASR_MODEL_PATH` 为已有本地模型目录。模型缺失时标记 `UNAVAILABLE`，不会调用云端 ASR。

浏览器提交使用异步案件任务；接口字段见 [本机 API v1](docs/local_api_v1.md)。Agent 默认使用已完整安装的本机 Qwen 模型；模型选型和效果以阶段报告为准。
本机权重来源与校验值见 [模型资产记录](docs/model_assets.md)。

默认仅监听 `127.0.0.1:5000`。`GET /health` 不需要云端密钥。需要云端推理时，按 `.env.example` 设置环境变量；旧版远程媒体 URL 端点默认拒绝所有主机，只有显式配置 HTTPS 主机白名单才会下载。请先轮换原压缩包中出现过的第三方凭据。

## 阶段记录

- [W0 基线修复](reports/W0-baseline.md)
- [W1 本地媒体与隐私基础](reports/W1-local-media-privacy.md)
- [W2 风险规则与可追溯检索](reports/W2-rules-rag.md)
- [W3 受控调查 Agent](reports/W3-agent.md)
- [W4 媒体取证与弃权](reports/W4-media-forensics.md)
- [W5 集成、浏览器验证与对照烟测](reports/W5-integration-evaluation.md)

后续实现以 [最终详细实施方案](../../outputs/金睛鉴诈_最终详细实施方案.md) 的阶段门槛为准。尚未完成的能力不可在演示中标记为已实现。
