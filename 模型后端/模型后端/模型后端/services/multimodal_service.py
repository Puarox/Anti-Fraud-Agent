import os
import cv2
import time
import json
import base64
import hashlib
import hmac
import urllib
import requests
import subprocess
import tempfile
from .safe_download import download_media
from .llm_service import get_client

# ================== 讯飞配置 ==================
APP_ID = os.getenv("XFYUN_APP_ID", "")
API_KEY = os.getenv("XFYUN_API_KEY", "")
LFASR_HOST = 'https://raasr.xfyun.cn/v2/api'
API_UPLOAD = '/upload'
API_GET_RESULT = '/getResult'


# ================== 下载工具 ==================
def download_file(url, suffix):
    return download_media(url, suffix)


def download_video(url):
    return download_file(url, ".mp4")


def download_audio(url):
    return download_file(url, ".mp3")


# ================== 图像 ==================
def image_to_text(image_url):
    completion = get_client().chat.completions.create(
        model="qwen-vl-plus",
        messages=[{
            "role": "user",
            "content": [
                {"type": "image_url", "image_url": {"url": image_url}},
                {"type": "text", "text": "详细描述图片内容，并判断是否存在诈骗"}
            ]
        }]
    )
    return completion.choices[0].message.content


# ================== 视频抽帧 ==================
def extract_frames(video_path, max_frames=4):
    cap = cv2.VideoCapture(video_path)
    frames = []

    total = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    step = max(total // max_frames, 1)

    for i in range(0, total, step):
        cap.set(cv2.CAP_PROP_POS_FRAMES, i)
        ret, frame = cap.read()
        if not ret:
            break

        _, buffer = cv2.imencode(".jpg", frame)
        frame_base64 = base64.b64encode(buffer).decode("utf-8")
        frames.append(f"data:image/jpeg;base64,{frame_base64}")

        if len(frames) >= max_frames:
            break

    cap.release()
    return frames


# ================== 视频提取音频 ==================
def video_to_audio(video_path):
    tmp_audio = tempfile.NamedTemporaryFile(delete=False, suffix=".wav").name

    command = [
        "ffmpeg",
        "-y",
        "-i", video_path,
        "-ac", "1",
        "-ar", "16000",
        tmp_audio
    ]

    subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    return tmp_audio


# ================== 语音识别 ==================
def audio_to_text(audio_path):
    if not APP_ID or not API_KEY:
        raise RuntimeError("XFYUN_APP_ID and XFYUN_API_KEY must be configured")
    ts = str(int(time.time()))

    m2 = hashlib.md5()
    m2.update((APP_ID + ts).encode('utf-8'))
    md5 = m2.hexdigest().encode('utf-8')

    signa = hmac.new(API_KEY.encode('utf-8'), md5, hashlib.sha1).digest()
    signa = base64.b64encode(signa).decode('utf-8')

    file_len = os.path.getsize(audio_path)
    file_name = os.path.basename(audio_path)

    param = {
        'appId': APP_ID,
        'signa': signa,
        'ts': ts,
        "fileSize": file_len,
        "fileName": file_name,
        "duration": "200"
    }

    with open(audio_path, 'rb') as f:
        data = f.read()

    upload_url = LFASR_HOST + API_UPLOAD + "?" + urllib.parse.urlencode(param)

    res = requests.post(upload_url, headers={"Content-type": "application/json"}, data=data, timeout=30).json()

    if res.get("code") != "000000":
        raise Exception(f"上传失败: {res}")

    order_id = res["content"]["orderId"]

    # 轮询
    deadline = time.monotonic() + 180
    while time.monotonic() < deadline:
        query_param = {
            'appId': APP_ID,
            'signa': signa,
            'ts': ts,
            'orderId': order_id,
            'resultType': "transfer"
        }

        query_url = LFASR_HOST + API_GET_RESULT + "?" + urllib.parse.urlencode(query_param)
        resp = requests.post(query_url, timeout=15).json()

        status = resp["content"]["orderInfo"]["status"]

        if status == 4:
            break

        time.sleep(2)
    else:
        raise TimeoutError("Speech recognition timed out")

    # 解析
    result_str = resp["content"]["orderResult"]
    data = json.loads(result_str)

    text = ""
    for item in data["lattice"]:
        json_1best = json.loads(item["json_1best"])
        for rt in json_1best["st"]["rt"]:
            for ws in rt["ws"]:
                for cw in ws["cw"]:
                    w = cw["w"]
                    if w.strip():
                        text += w
    return text.strip()


# ================== 多模态分析（核心） ==================
def video_multimodal_analysis(video_path):
    try:

        # 1. 抽帧
        frames = extract_frames(video_path)

        # 2. 提取音频
        audio_path = video_to_audio(video_path)

        # 3. 语音识别
        try:
            speech_text = audio_to_text(audio_path)
        except Exception:
            speech_text = "语音识别失败"

        # 4. 多模态融合
        completion = get_client().chat.completions.create(
            model="qwen-vl-max-latest",
            messages=[{
                "role": "user",
                "content": [
                    {"type": "video", "video": frames},
                    {
                        "type": "text",
                        "text": f"""
【语音内容】
{speech_text}

请结合画面 + 语音：
1. 描述视频内容
2. 是否存在诈骗
3. 诈骗类型
4. 风险等级（低/中/高）
5. 防范建议
"""
                    }
                ]
            }]
        )

        return completion.choices[0].message.content

    finally:
        # 清理临时文件（非常关键）
        try:
            os.remove(video_path)
        except:
            pass


# ================== ⭐统一入口（推荐你用这个） ==================
def analyze_video_from_url(video_url):
    video_path = download_video(video_url)

    try:
        return video_multimodal_analysis(video_path)
    finally:
        try:
            os.remove(video_path)
        except:
            pass

