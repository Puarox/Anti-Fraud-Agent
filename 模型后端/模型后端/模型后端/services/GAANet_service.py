import os
import subprocess
import uuid
import tempfile


def gaanet_infer(video_path: str) -> str:
    """
    调用 GAANet 进行视频语音识别
    """

    # 输出文件（GAANet一般会输出到某个txt）
    output_txt = os.path.join(tempfile.gettempdir(), f"{uuid.uuid4()}.txt")

    try:
        # ⚠️ 根据你项目实际路径调整
        cmd = [
            "python",
            "evl_single.py",
            "--video_path", video_path,
            "--output", output_txt
        ]

        result = subprocess.run(
            cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )

        if result.returncode != 0:
            raise Exception(result.stderr)

        # 读取结果
        if os.path.exists(output_txt):
            with open(output_txt, "r", encoding="utf-8") as f:
                text = f.read().strip()
        else:
            # fallback：有些实现直接print
            text = result.stdout.strip()

        return text

    except Exception as e:
        raise Exception(f"GAANet推理失败: {str(e)}")

    finally:
        if os.path.exists(output_txt):
            os.remove(output_txt)