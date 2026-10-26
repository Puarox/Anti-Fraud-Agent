# 本地模型来源与版本记录

以下目录在 `.gitignore` 中排除，源码工作区只保存调用方式和验收记录。模型及权重应从原作者仓库取得，部署时核对许可证和 SHA-256。

| 用途 | 来源 | 本机相对路径 | 本次 SHA-256 |
|---|---|---|---|
| 离线规划 | [Qwen2.5-1.5B-Instruct](https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct)，Apache-2.0 | `models/qwen2.5-1.5b-instruct/model.safetensors` | `DD924A11B4C220F385B51FFA522DAEA7C9F3D850E31B162BB5661DF483C6D3EE` |
| 视频人脸实验信号 | [DFDS EfficientNet-B2](https://huggingface.co/ThinothW/DFDS-EfficientNet-B2-Deepfake-Detection-Engine)，Apache-2.0 | `models/dfds-b2/pytorch_model.bin` | `444EFD63DD3A312EE3515B608AC238C7B748C75A1E7D9CD3B9C19343694A2279` |
| 音频反伪造实验信号 | [AASIST](https://github.com/clovaai/aasist)，MIT | `third_party/aasist/models/weights/AASIST.pth` | `51D2D9CF0738172F61E2A384EC50A54A55363240F67C971ED55A92435BC1A1C0` |
| 本地中文 ASR | [Systran/faster-whisper-base](https://huggingface.co/Systran/faster-whisper-base) | `models/faster-whisper-base/model.bin` | `D01C3014881C9C6F3133C182F3D2887EB6CA1C789A7538C5C007196857A0A6A9` |
| 中文向量检索 | [BAAI/bge-small-zh-v1.5](https://huggingface.co/BAAI/bge-small-zh-v1.5) | `models/bge-small-zh-v1.5/` | 目录需逐文件固定版本 |

0.5B 回退模型来自 [Qwen2.5-0.5B-Instruct](https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct)。本机 1.5B 权重约 3.09 GB；内存约 16 GB 的工作站可以运行，但 CPU 单步延迟需看 W5 实测。缺少候选媒体模型时返回 `UNAVAILABLE`，不会伪装成已检测。
