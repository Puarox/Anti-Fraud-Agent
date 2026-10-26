"""Fast, explainable fraud indicators. A rule hit is a warning, not a legal finding."""

from __future__ import annotations

import re


RULES = [
    {
        "id": "SAFE_ACCOUNT_TRANSFER",
        "pattern": re.compile(r"安全账户.{0,28}(?:转账|汇款|打款)|(?:转账|汇款|打款).{0,28}安全账户"),
        "level": "CRITICAL",
        "advice": "暂停转账，通过独立的官方渠道核实身份。",
        "source_doc": "cac_2022_high_frequency",
    },
    {
        "id": "SCREEN_SHARE_CREDENTIAL",
        "pattern": re.compile(r"(?:共享屏幕|屏幕共享|远程控制).{0,50}(?:验证码|密码|银行卡|转账)|(?:验证码|密码|银行卡|转账).{0,50}(?:共享屏幕|屏幕共享|远程控制)"),
        "level": "HIGH",
        "advice": "停止屏幕共享，不输入验证码或密码，并从官方渠道核验对方身份。",
        "source_doc": "nfra_2024_screen_ai",
    },
    {
        "id": "BRUSHING_PREPAY",
        "pattern": re.compile(r"(?:刷单|返利|做任务).{0,40}(?:垫资|充值|先交钱|解冻费)|(?:垫资|充值|解冻费).{0,40}(?:刷单|返利|做任务)"),
        "level": "HIGH",
        "advice": "停止垫资或充值，保留聊天和交易记录。",
        "source_doc": "miit_2026_brushing",
    },
    {
        "id": "ASK_OTP",
        "pattern": re.compile(r"(?:索要|提供|告诉|发给|输入).{0,14}验证码|验证码.{0,14}(?:发给|告诉|提供|输入)"),
        "level": "MEDIUM",
        "advice": "不要向对方提供验证码；从原平台或机构官方渠道核验。",
        "source_doc": "nfra_2024_screen_ai",
    },
]

NEGATION = re.compile(r"不要|拒绝|切勿|别给|别告诉|不提供|停止")
ORDER = {"UNKNOWN": 0, "MEDIUM": 1, "HIGH": 2, "CRITICAL": 3}


def evaluate_rules(observations: list[dict]) -> dict:
    hits = []
    for rule in RULES:
        for observation in observations:
            text = observation.get("text", "")
            match = rule["pattern"].search(text)
            if not match:
                continue
            window = text[max(0, match.start() - 4):match.end() + 4]
            if NEGATION.search(window):
                continue
            hits.append({
                "rule_id": rule["id"],
                "level": rule["level"],
                "evidence_ids": [observation["evidence_id"]],
                "matched_text": match.group(0),
                "advice": rule["advice"],
                "source_evidence_id": "kb_" + rule["source_doc"],
            })
    level = max((hit["level"] for hit in hits), key=ORDER.get, default="UNKNOWN")
    return {"level": level, "hits": hits,
            "meaning": "warning_indicators_only" if hits else "no_rule_hit_is_not_safe"}
