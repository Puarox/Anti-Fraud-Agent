"""Victim-facing incident response and reporting preparation tools."""

from __future__ import annotations

from urllib.parse import urlsplit


RESPONSE_SOURCES = [
    {
        "title": "上海警方：发现被骗后应该怎么做",
        "agency": "上海市公安局",
        "url": "https://www.shanghai.gov.cn/nw31406/20250527/ab65c2b8613447a9bcd8535363369efd.html",
        "published_at": "2025-05-22",
    },
    {
        "title": "警惕利用屏幕共享等实施新型电信网络诈骗",
        "agency": "国家金融监督管理总局云南监管局",
        "url": "https://www.nfra.gov.cn/branch/yunnan/view/pages/common/ItemDetail.html?docId=1226950&itemId=1952",
        "published_at": "2025-09-24",
    },
]


def _signal_ids(facts: list[dict], markers: tuple[str, ...]) -> list[str]:
    return [
        fact.get("evidence_id")
        for fact in facts
        if fact.get("evidence_id") and any(marker in str(fact.get("text", "")) for marker in markers)
    ]


def build_response_playbook(facts: list[dict]) -> dict:
    """Return conditional, source-backed actions without deciding what happened."""
    payment_ids = _signal_ids(facts, ("转账", "汇款", "打款", "扣款", "盗刷", "付款", "充值"))
    access_ids = _signal_ids(facts, ("验证码", "动态码", "密码", "银行卡", "卡号"))
    remote_ids = _signal_ids(facts, ("屏幕共享", "共享屏幕", "远程控制", "远程操控", "远程协助", "黑屏"))
    contact_ids = _signal_ids(facts, ("客服", "公检法", "退款", "安全账户", "验证码"))
    branches = [
        {
            "scenario": "仍在通话或对方要求继续操作",
            "condition": "如果对方仍在联系，或正在要求转账、提供验证码、安装软件或开启共享屏幕：",
            "steps": [
                "停止转账和屏幕共享，不再提供验证码、密码或银行卡信息。",
                "结束与对方的通话/聊天；不要拨打对方提供的号码或点击其发来的链接。",
                "通过自己打开的官方 App、银行卡背面电话或机构官网独立核实。",
            ],
            "evidence_ids": contact_ids,
            "source_urls": [RESPONSE_SOURCES[0]["url"], RESPONSE_SOURCES[1]["url"]],
        },
        {
            "scenario": "已经转账或发现不明交易",
            "condition": "如果已经转账、发现账户有本人未操作的交易，或泄露了支付密码/验证码：",
            "steps": [
                "尽快使用银行/支付机构的官方 App 或官方客服电话联系开户机构，说明疑似诈骗并申请紧急止付、挂失或冻结；不要等 Agent 判断。",
                "尽快拨打 110 或到就近公安机关报案，说明转账时间、金额、渠道和收款信息。止付与追回结果由银行和公安机关处理，不能保证追回。",
                "保存聊天、来电、短信、交易回执和涉诈网址/应用等原始记录，不要删掉对话。",
            ],
            "evidence_ids": sorted(set(payment_ids + access_ids)),
            "source_urls": [RESPONSE_SOURCES[0]["url"]],
        },
        {
            "scenario": "开启屏幕共享或手机疑似被远程控制",
            "condition": "如果已经安装陌生软件、开启共享屏幕，或手机出现非本人操作：",
            "steps": [
                "尽量改用另一台可信设备联系银行和公安机关；若原手机仍在被控制，按警方/银行指引断开网络或关机。",
                "在可信设备上联系银行检查并保护账户，再修改重要账户密码；不要在疑似被控制的手机上继续输入密码。",
                "保留软件名称、来电/聊天记录和异常交易记录；不要为了清理手机先删除证据。",
            ],
            "evidence_ids": remote_ids,
            "source_urls": [RESPONSE_SOURCES[1]["url"]],
        },
    ]
    signal_ids = sorted(set(payment_ids + access_ids + remote_ids + contact_ids))
    return {
        "branches": branches,
        "matched_case_evidence_ids": signal_ids,
        "sources": RESPONSE_SOURCES,
        "notice": "各项处置均为条件式提示；关键词命中不代表该事件已发生。紧急止付、账户冻结和报案请直接联系银行或公安机关。",
    }


def prepare_reporting_checklist(facts: list[dict], rules: dict,
                                url_checks: list[dict], media_sha256: str | None) -> dict:
    """Prepare a checklist for the user; never files or sends a report."""
    evidence = [{"evidence_id": item.get("evidence_id"), "type": item.get("type", "unknown"),
                 "at_ms": item.get("at_ms", item.get("start_ms"))}
                for item in facts if item.get("evidence_id")]
    hosts = set()
    for item in url_checks:
        if not item.get("url"):
            continue
        try:
            host = urlsplit(item["url"]).hostname
        except ValueError:
            host = None
        if host:
            hosts.add(host)
    matched_rules = [{"rule_id": hit.get("rule_id"),
                      "evidence_ids": hit.get("evidence_ids", [])}
                     for hit in rules.get("hits", [])]
    return {
        "available_case_material": {
            "evidence": evidence,
            "url_hosts": sorted(hosts),
            "risk_rule_hits": matched_rules,
            "media_sha256": media_sha256,
        },
        "details_to_prepare": [
            "首次联系时间、对方使用的电话/账号/平台，以及对方自称的身份。",
            "如有转账：金额、时间、支付渠道、收款账户/商户和交易回执。不要在此处或向任何人提供密码、支付 PIN 或验证码。",
            "是否安装陌生应用、共享屏幕、透露验证码/密码/银行卡信息，以及发现异常的时间。",
            "联系银行/支付机构和公安机关的时间、受理编号或后续要求（如已有）。",
        ],
        "preserve_originals": [
            "聊天记录、来电/短信、转账回执、银行流水、网址和应用信息；保留原件与时间信息。",
            "媒体 SHA-256 仅用于区分本地文件，不代表媒体内容真实。原始文件不要只保留脱敏预览。",
        ],
        "sources": [RESPONSE_SOURCES[0]],
        "notice": "此工具只整理准备清单，不会联系银行、提交报案或上传案件资料。请通过银行和公安机关的官方渠道提供所需材料。",
    }
