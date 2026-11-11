"""Local, auditable human-review queue for cases needing a person."""

from __future__ import annotations

import json
import sqlite3
import uuid
from contextlib import contextmanager
from datetime import UTC, datetime
from pathlib import Path

from .cases import BASE_DIR
from .privacy import Redactor


DB_PATH = BASE_DIR / "review_queue.sqlite"


def _connect() -> sqlite3.Connection:
    DB_PATH.parent.mkdir(parents=True, exist_ok=True)
    connection = sqlite3.connect(DB_PATH, timeout=10)
    connection.row_factory = sqlite3.Row
    connection.execute("""CREATE TABLE IF NOT EXISTS review_tickets (
        ticket_id TEXT PRIMARY KEY, case_id TEXT NOT NULL, status TEXT NOT NULL,
        priority TEXT NOT NULL, reason TEXT NOT NULL, evidence_ids TEXT NOT NULL,
        created_at TEXT NOT NULL, updated_at TEXT NOT NULL, reviewer_note TEXT NOT NULL DEFAULT ''
    )""")
    connection.execute("CREATE INDEX IF NOT EXISTS idx_review_status ON review_tickets(status, created_at)")
    return connection


@contextmanager
def _db():
    connection = _connect()
    try:
        yield connection
        connection.commit()
    finally:
        connection.close()


def create_ticket(case_id: str, reason: str, evidence_ids: list[str] | None = None,
                  priority: str = "NORMAL") -> dict:
    priority = priority if priority in {"LOW", "NORMAL", "HIGH", "URGENT"} else "NORMAL"
    safe_reason = Redactor().redact(str(reason).strip())[:500] or "Agent 请求人工复核"
    ids = sorted({str(item)[:80] for item in (evidence_ids or []) if item})
    now = datetime.now(UTC).isoformat()
    with _db() as connection:
        existing = connection.execute(
            "SELECT * FROM review_tickets WHERE case_id=? AND status IN ('OPEN','IN_REVIEW') ORDER BY created_at LIMIT 1",
            (case_id,),
        ).fetchone()
        if existing:
            return _to_dict(existing, duplicate=True)
        ticket_id = str(uuid.uuid4())
        connection.execute(
            "INSERT INTO review_tickets(ticket_id,case_id,status,priority,reason,evidence_ids,created_at,updated_at) VALUES(?,?,?,?,?,?,?,?)",
            (ticket_id, case_id, "OPEN", priority, safe_reason, json.dumps(ids), now, now),
        )
        row = connection.execute("SELECT * FROM review_tickets WHERE ticket_id=?", (ticket_id,)).fetchone()
        return _to_dict(row)


def list_tickets(status: str | None = None, limit: int = 100) -> list[dict]:
    with _db() as connection:
        if status in {"OPEN", "IN_REVIEW", "RESOLVED", "CLOSED"}:
            rows = connection.execute("SELECT * FROM review_tickets WHERE status=? ORDER BY created_at DESC LIMIT ?",
                                      (status, min(max(limit, 1), 500))).fetchall()
        else:
            rows = connection.execute("SELECT * FROM review_tickets ORDER BY created_at DESC LIMIT ?",
                                      (min(max(limit, 1), 500),)).fetchall()
        return [_to_dict(row) for row in rows]


def update_ticket(ticket_id: str, status: str, reviewer_note: str = "") -> dict | None:
    if status not in {"IN_REVIEW", "RESOLVED", "CLOSED", "OPEN"}:
        raise ValueError("Unsupported review status")
    if status == "RESOLVED" and not str(reviewer_note).strip():
        raise ValueError("A reviewer note is required before resolving a ticket")
    now = datetime.now(UTC).isoformat()
    safe_note = Redactor().redact(str(reviewer_note).strip())[:1000]
    with _db() as connection:
        cursor = connection.execute(
            "UPDATE review_tickets SET status=?, reviewer_note=?, updated_at=? WHERE ticket_id=?",
            (status, safe_note, now, ticket_id),
        )
        if not cursor.rowcount:
            return None
        return _to_dict(connection.execute("SELECT * FROM review_tickets WHERE ticket_id=?", (ticket_id,)).fetchone())


def _to_dict(row: sqlite3.Row, duplicate: bool = False) -> dict:
    result = dict(row)
    result["evidence_ids"] = json.loads(result["evidence_ids"])
    result["duplicate_open_ticket"] = duplicate
    return result
