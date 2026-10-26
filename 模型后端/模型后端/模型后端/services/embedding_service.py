import os
import hashlib
import json
from openai import OpenAI

# 初始化 Qwen client
def get_client() -> OpenAI:
    key = os.getenv("QWEN_API_KEY")
    if not key:
        raise RuntimeError("QWEN_API_KEY must be configured for cloud embeddings")
    return OpenAI(
        api_key=key,
        base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
    )

# 简单文件缓存（也可以换 Redis）
CACHE_PATH = "./embedding_cache.json"

# 加载缓存
if os.path.exists(CACHE_PATH):
    with open(CACHE_PATH, "r", encoding="utf-8") as f:
        cache = json.load(f)
else:
    cache = {}


def get_cache_key(text: str):
    return hashlib.md5(text.encode("utf-8")).hexdigest()


def save_cache():
    with open(CACHE_PATH, "w", encoding="utf-8") as f:
        json.dump(cache, f, ensure_ascii=False)


def get_embedding(text: str):
    key = get_cache_key(text)

    # ✅ 命中缓存
    if key in cache:
        return cache[key]

    # ❗ 调用 embedding API（Qwen）
    resp = get_client().embeddings.create(
        model="text-embedding-v1",
        input=text
    )

    embedding = resp.data[0].embedding

    # 写缓存
    cache[key] = embedding
    save_cache()

    return embedding
