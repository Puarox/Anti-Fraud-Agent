"""Opt-in local folder guardian. Run with python -m jinjing.guardian --watch-dir PATH."""

from __future__ import annotations

import argparse
import shutil
import sys
import time
from pathlib import Path

from werkzeug.datastructures import FileStorage

from .agent import Investigator, LocalQwenPlanner
from .cases import BASE_DIR, EXTENSIONS, create_case
from .review_queue import create_ticket


def run(watch_dir: Path, poll_seconds: float = 2.0) -> None:
    watch_dir = watch_dir.expanduser().resolve()
    if not watch_dir.is_dir():
        raise SystemExit(f"Watch directory does not exist: {watch_dir}")
    archive = watch_dir / "_jinjing_processed"
    failed = watch_dir / "_jinjing_failed"
    archive.mkdir(exist_ok=True)
    failed.mkdir(exist_ok=True)
    print(f"Guardian is watching only: {watch_dir}", flush=True)
    print("Drop supported evidence here; Ctrl+C stops monitoring.", flush=True)
    stable: dict[Path, tuple[int, int]] = {}
    with Investigator(LocalQwenPlanner()) as investigator:
        try:
            while True:
                for path in watch_dir.iterdir():
                    if not path.is_file() or path.suffix.lower() not in EXTENSIONS:
                        continue
                    try:
                        stat = path.stat()
                    except OSError:
                        continue
                    signature = (stat.st_size, stat.st_mtime_ns)
                    if stable.get(path) != signature:
                        stable[path] = signature
                        continue
                    try:
                        if stat.st_size > 50 * 1024 * 1024:
                            raise ValueError("Evidence exceeds the 50 MiB intake limit")
                        with path.open("rb") as source:
                            manifest = create_case("", FileStorage(stream=source, filename=path.name))
                        report = investigator.run(manifest)
                        if manifest.get("risk_indicators", {}).get("level") in {"HIGH", "CRITICAL"}:
                            create_ticket(manifest["case_id"],
                                          "本地守护进程发现高风险案件，等待人工复核",
                                          [fact.get("evidence_id") for fact in manifest.get("observations", [])
                                           if fact.get("evidence_id")],
                                          "URGENT" if manifest["risk_indicators"]["level"] == "CRITICAL" else "HIGH")
                        destination = archive / path.name
                        if destination.exists():
                            destination = archive / f"{int(time.time())}_{path.name}"
                        shutil.move(str(path), str(destination))
                        print(f"case={manifest['case_id']} risk={report['fraud_risk']['level']} file={destination.name}",
                              flush=True)
                    except Exception as exc:
                        destination = failed / path.name
                        if path.exists():
                            if destination.exists():
                                destination = failed / f"{int(time.time())}_{path.name}"
                            shutil.move(str(path), str(destination))
                        print(f"failed={path.name} reason={type(exc).__name__}", file=sys.stderr, flush=True)
                    finally:
                        stable.pop(path, None)
                time.sleep(max(0.5, poll_seconds))
        except KeyboardInterrupt:
            print("Guardian stopped.", flush=True)


def main() -> None:
    parser = argparse.ArgumentParser(description="Opt-in local anti-fraud evidence folder guardian")
    parser.add_argument("--watch-dir", required=True, type=Path,
                        help="The only directory this process will monitor")
    parser.add_argument("--poll-seconds", type=float, default=2.0)
    args = parser.parse_args()
    run(args.watch_dir, args.poll_seconds)


if __name__ == "__main__":
    main()
