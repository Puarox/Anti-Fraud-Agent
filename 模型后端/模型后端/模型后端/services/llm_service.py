import os
from openai import OpenAI

def get_client() -> OpenAI:
    key = os.getenv("QWEN_API_KEY")
    if not key:
        raise RuntimeError("QWEN_API_KEY must be configured for cloud inference")
    return OpenAI(
        api_key=key,
        base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
    )

def chat(prompt):
    completion = get_client().chat.completions.create(
        model="qwen-plus",
        messages=[{"role": "user", "content": prompt}],
    )
    return completion.choices[0].message.content
