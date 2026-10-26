from flask import Flask, request, jsonify
import os

from services.diagnose_service import diagnose
from services.download_audio import download_audio
from services.multimodal_service import image_to_text, audio_to_text, video_multimodal_analysis, analyze_video_from_url
from services.rag_service import init_rag
from services.safe_download import UnsafeMediaURL

app = Flask(__name__)


@app.get("/health")
def health():
    return jsonify({"status": "ok", "cloud_llm_configured": bool(os.getenv("QWEN_API_KEY"))})

@app.route("/diagnose/rag/init", methods=["POST"])
def rag_init_api():
    try:
        data = request.get_json(silent=True) or {}
        cases = data.get("cases", [])

        if not cases:
            return jsonify({
                "code": 400,
                "msg": "cases不能为空"
            }), 400

        count = init_rag(cases)

        return jsonify({
            "code": 200,
            "msg": "初始化成功",
            "count": count
        })

    except Exception as e:
        return jsonify({
            "code": 500,
            "msg": str(e)
        }), 500



# 1 文本接口
@app.route("/diagnose/text", methods=["POST"])
def diagnose_text():
    data = request.get_json(silent=True) or {}
    text = data.get("text")
    if not isinstance(text, str) or not text.strip():
        return jsonify({"code": 400, "msg": "text不能为空"}), 400

    result = diagnose(text)
    return jsonify(result)


# 2 图片接口
@app.route("/diagnose/image", methods=["POST"])
def diagnose_image():
    data = request.get_json(silent=True) or {}

    text = data.get("text", "")
    image_url = data.get("image_url")
    if not isinstance(image_url, str) or not image_url:
        return jsonify({"code": 400, "msg": "image_url不能为空"}), 400

    image_desc = image_to_text(image_url)

    final_text = text + "\n" + image_desc
    result = diagnose(final_text)

    return jsonify(result)


@app.route("/diagnose/video", methods=["POST"])
def diagnose_video():
    data = request.get_json(silent=True) or {}

    text = data.get("text", "")
    video_url = data.get("video_url")

    if not video_url:
        return jsonify({
            "code": 400,
            "msg": "video_url不能为空"
        }), 400

    try:
        # ✅ 直接调用统一入口
        video_desc = analyze_video_from_url(video_url)

        final_text = text + "\n" + video_desc
        result = diagnose(final_text)

        return jsonify(result)

    except UnsafeMediaURL as e:
        return jsonify({"code": 400, "msg": str(e)}), 400
    except Exception:
        return jsonify({
            "code": 500,
            "msg": "视频分析失败，请检查服务配置和日志"
        }), 500
@app.route("/diagnose/video_gaanet", methods=["POST"])
def diagnose_video_gaanet():
    return jsonify({
        "code": 410,
        "msg": "GAANet is an audio-visual speech separation model, not a fraud or deepfake detector",
    }), 410


@app.route("/diagnose/audio", methods=["POST"])
def diagnose_audio():
    data = request.get_json(silent=True) or {}

    text = data.get("text", "")
    audio_url = data.get("audio_url")

    if not isinstance(audio_url, str) or not audio_url:
        return jsonify({"code": 400, "msg": "audio_url不能为空"}), 400
    try:
        audio_path = download_audio(audio_url)
        audio_text = audio_to_text(audio_path)
        return jsonify(diagnose(text + "\n" + audio_text))
    except UnsafeMediaURL as e:
        return jsonify({"code": 400, "msg": str(e)}), 400
    except Exception:
        return jsonify({"code": 500, "msg": "音频分析失败，请检查服务配置和日志"}), 500
    finally:
        if "audio_path" in locals() and os.path.exists(audio_path):
            os.remove(audio_path)

if __name__ == "__main__":
    app.run(host="127.0.0.1", debug=False, port=5000)
