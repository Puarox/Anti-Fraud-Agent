# W4 媒体取证阶段报告（2026-10-25）

## 交付

- 案件创建时本地检查 C2PA 凭证、FFprobe 质量信息和时间长度；无凭证只记 `ABSENT`，不判伪。
- 本地运行 [AASIST](https://github.com/clovaai/aasist) 官方 ASVspoof2019 LA 权重，对音频片段记录原始 bona fide logit 和毫秒范围。音频不足 2 秒、接近静音时弃权。
- 视频抽帧后，用 [DFDS EfficientNet-B2](https://huggingface.co/ThinothW/DFDS-EfficientNet-B2-Deepfake-Detection-Engine) 权重在人脸区域运行实验检测，记录帧时间、人脸框和原始 softmax。没有合适人脸则弃权；最多检测 8 帧。
- 将三类信号写入案件清单和调查报告，Web 工作台单独展示“行为诈骗风险”和“媒体真实性”。真实性始终为 `UNKNOWN`，目前没有通过本项目域内校准的真伪阈值。
- [C2PA Python SDK](https://github.com/contentauth/c2pa-python/blob/main/docs/usage.md) 以离线模式运行，不远程抓取凭证或 OCSP。

## 实测

- 4 秒本地合成音视频：C2PA `ABSENT`，质量正常但高度 220px 被标为低分辨率，AASIST 返回原始分数，视频无人脸返回 `UNKNOWN`。
- `skimage` 自带真实 astronaut 人像：DFDS 原始 fake softmax 为 0.93375。此样本非目标域，且当前 OpenCV Haar 裁脸与作者 RetinaFace 推理流程不同；该结果是明确的误报风险证据，不能显示为 93% 伪造概率。
- 3 秒静音测试：AASIST 弃权，整体真实性仍 `UNKNOWN`。
- W0–W3 原有 14 个测试继续通过；新增 W4 弃权测试通过。

## 未通过的验收门槛

视频和语音模型均未在手机录屏、社交平台压缩、方言、变声、真实诈骗视频与合法合成视频的独立分域集合上校准。当前视频裁脸实现和原模型流程不同，分数只可用于研发排查。C2PA 无凭证、取证分数及诈骗行为三者没有因果等价关系。分域测试和误报率/漏报率须在数据集建成后报告，当前不能作为公共服务自动真伪结论。

## 复现

模型文件默认放于工作区 `models/dfds-b2/pytorch_model.bin`；AASIST 官方仓库默认在 `third_party/aasist`。二者权重与代码需要按各自许可证保留来源，模型目录不纳入源码。运行 `python -m unittest discover -s tests -v`。服务入口 `python local_web.py`。
