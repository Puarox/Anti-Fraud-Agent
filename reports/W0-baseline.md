# W0：基线修复记录

日期：2026-10-25。

## 已完成

- 从原 RAR 的源码复制独立工作区，保留原件。
- 修复 `services.gaanet_service` 大小写导入错误及 Flask `diagnose_video` 重名导致的启动失败。
- 将误用的 GAANet 视频鉴诈入口改为 410；其原仓库任务是音视觉语音分离。
- 从工作副本删除硬编码讯飞凭据，改由 `XFYUN_APP_ID/XFYUN_API_KEY` 提供。Qwen 客户端改为调用时创建，未配置密钥也可启动本地 API。
- 旧 URL 媒体下载默认拒绝远程主机；配置精确 HTTPS 白名单后才允许，且限制跳转、大小和请求超时。
- ASR 轮询增加截止时间，音频临时文件在接口完成后清理；服务默认只监听环回地址且关闭 Flask debug。
- 移除重复的 `services/app.py` 入口，以根目录 `app.py` 为唯一旧版入口。

## 验证

- 无云端密钥时成功导入 `app`，`GET /health` 返回 200。
- 空文本和空音频请求返回 400，GAANet 旧入口返回 410，未白名单媒体 URL 返回 400。
- `python -m unittest discover -s tests -v`：3 项通过。
- `python -m compileall -q app.py services tests`：通过。

## 尚未完成与限制

- 本机未配置可用于诊断的云端 API 密钥，因此未运行真实模型调用；不能据此报告鉴诈准确率。
- 原始 RAR 中出现过的第三方凭据必须由持有人在服务商处轮换；从工作副本删除不能使旧凭据失效。
- 旧 URL 下载仅为保守的演示门禁。下一阶段以本地文件上传取代它；多用户生产部署仍需网络层出站限制和隔离。
- 旧 RAG 仍是 Chroma Top-3；W2 改造。
