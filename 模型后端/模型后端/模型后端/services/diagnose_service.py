import json

from .rag_service import retrieve
from .llm_service import chat

def diagnose(text):
    rag = retrieve(text)
    cases = rag["documents"]
    ids = rag["ids"]

    prompt = f"""
你是一个反诈骗分析助手，请基于用户输入和相关案例进行判断。

【用户输入】
{text}

【相关案例】
{cases}

【任务要求】
请完成以下分析：
1. 判断是否为诈骗
2. 诈骗类型
3. 风险等级
4. 简要理由
5. 简要建议

【严格输出要求】
⚠️ 只允许输出 JSON，禁止输出任何解释、前后缀、markdown、代码块标记（如```json）

⚠️ 字段必须完全匹配如下格式与枚举：

- is_fraud: 只能是 "YES" / "NO" / "SUSPICIOUS"
- fraud_type: 字符串（不超过20字）
- risk_level: 只能是 "LOW" / "MEDIUM" / "HIGH" / "CRITICAL"
- reason: 不超过100字
- advice: 不超过100字

⚠️ 推理必须简洁，总体输出内容尽量控制在200字以内

【输出格式】
{{
    "is_fraud": "YES | NO | SUSPICIOUS",
    "fraud_type": "",
    "risk_level": "LOW | MEDIUM | HIGH | CRITICAL",
    "reason": "",
    "advice": ""
}}
"""

    raw = chat(prompt)   # ✅ 修复点

    try:
        result = json.loads(raw)
        print(result)
    except:
        result = {
            "is_fraud": "未知",
            "fraud_type": "未知",
            "risk_level": "未知",
            "reason": raw,
            "advice": "模型输出解析失败"
        }

    return {
        "result": result,
        "case_id": ids[0] if ids else None
    }