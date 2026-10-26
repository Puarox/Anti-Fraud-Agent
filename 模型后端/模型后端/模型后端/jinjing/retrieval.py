"""Source-aware, offline hybrid retrieval for reviewed anti-fraud guidance."""

from __future__ import annotations

import json
import math
import os
from collections import Counter
from datetime import date
from pathlib import Path

import jieba
import numpy as np


SEED_PATH = Path(__file__).resolve().parents[1] / "knowledge" / "seed.json"
DEFAULT_MODEL = Path(__file__).resolve().parents[4] / "models" / "bge-small-zh-v1.5"
EXPANSIONS = {
    "屏幕共享": ["共享屏幕", "远程控制"],
    "共享屏幕": ["屏幕共享", "远程控制"],
    "安全账户": ["公检法", "转账"],
    "刷单": ["垫资", "返利", "任务"],
    "换脸": ["拟声", "冒充亲友"],
}


def _tokens(text: str) -> list[str]:
    return [token.lower().strip() for token in jieba.lcut(text) if token.strip()]


class LocalKnowledge:
    def __init__(self, path: Path = SEED_PATH) -> None:
        all_docs = json.loads(path.read_text(encoding="utf-8"))
        today = date.today().isoformat()
        self.docs = [doc for doc in all_docs
                     if doc["review_status"] == "SOURCE_VERIFIED"
                     and (not doc.get("valid_until") or doc["valid_until"] >= today)]
        self.tokens = [_tokens(doc["text"] + " " + " ".join(doc["keywords"])) for doc in self.docs]
        self.avg_length = sum(map(len, self.tokens)) / max(len(self.tokens), 1)
        self.df = Counter(token for words in self.tokens for token in set(words))
        self._model = None
        self._embeddings = None

    def _bm25(self, query_tokens: list[str]) -> list[float]:
        scores = []
        count = len(self.docs)
        for words in self.tokens:
            tf = Counter(words)
            length = len(words)
            score = 0.0
            for term in set(query_tokens):
                if term not in tf:
                    continue
                idf = math.log(1 + (count - self.df[term] + 0.5) / (self.df[term] + 0.5))
                score += idf * tf[term] * 2.2 / (tf[term] + 1.2 * (0.25 + 0.75 * length / self.avg_length))
            scores.append(score)
        return scores

    def _dense(self, query: str) -> list[float] | None:
        if not (DEFAULT_MODEL / "model.safetensors").exists() and not (DEFAULT_MODEL / "pytorch_model.bin").exists():
            return None
        if self._model is None:
            from sentence_transformers import SentenceTransformer
            self._model = SentenceTransformer(str(DEFAULT_MODEL), device="cpu", local_files_only=True)
            self._embeddings = self._model.encode(
                [doc["text"] for doc in self.docs], normalize_embeddings=True,
                convert_to_numpy=True,
            )
        q = self._model.encode(["为这个句子生成表示以用于检索相关文章：" + query],
                               normalize_embeddings=True, convert_to_numpy=True)[0]
        return (self._embeddings @ q).astype(float).tolist()

    def search(self, query: str, limit: int = 3) -> dict:
        expanded = query + " " + " ".join(
            word for trigger, words in EXPANSIONS.items() if trigger in query for word in words
        )
        lexical = self._bm25(_tokens(expanded))
        dense = self._dense(query)
        lex_rank = sorted(range(len(self.docs)), key=lambda i: lexical[i], reverse=True)
        dense_rank = sorted(range(len(self.docs)), key=lambda i: dense[i], reverse=True) if dense else []
        fused = {}
        for rank, index in enumerate(lex_rank):
            fused[index] = fused.get(index, 0) + 1 / (60 + rank + 1)
        for rank, index in enumerate(dense_rank):
            fused[index] = fused.get(index, 0) + 1 / (60 + rank + 1)
        # A lightweight exact-keyword rerank keeps critical terms visible.
        for index, doc in enumerate(self.docs):
            fused[index] += 0.005 * sum(term in query for term in doc["keywords"])
        order = sorted(fused, key=lambda i: fused[i], reverse=True)[:limit]
        return {
            "query": query,
            "expanded_query": expanded,
            "mode": "BM25_DENSE_RRF" if dense is not None else "BM25_ONLY",
            "results": [{
                "evidence_id": f"kb_{self.docs[i]['doc_id']}",
                "doc_id": self.docs[i]["doc_id"],
                "title": self.docs[i]["title"],
                "source_url": self.docs[i]["source_url"],
                "published_at": self.docs[i]["published_at"],
                "reviewed_at": self.docs[i]["reviewed_at"],
                "summary": self.docs[i]["text"],
                "score": round(fused[i], 5),
                "lexical_score": round(lexical[i], 4),
                "dense_score": round(dense[i], 4) if dense is not None else None,
            } for i in order],
        }


_knowledge = None


def get_knowledge() -> LocalKnowledge:
    global _knowledge
    if _knowledge is None:
        _knowledge = LocalKnowledge()
    return _knowledge
