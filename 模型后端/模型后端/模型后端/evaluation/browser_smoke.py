"""Headless local Web intake smoke test; run with the webapp-testing server helper."""

from pathlib import Path

from playwright.sync_api import sync_playwright


def main() -> None:
    screenshot = Path(__file__).resolve().parents[4] / "reports" / "w5-browser-evidence.png"
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(channel="msedge", headless=True)
        try:
            page = browser.new_page(viewport={"width": 1280, "height": 900})
            errors = []
            page.on("pageerror", lambda error: errors.append(str(error)))
            page.goto("http://127.0.0.1:5050/", wait_until="networkidle")
            page.locator("#text").fill("对方让我转账到安全账户，联系13800138000")
            page.get_by_role("button", name="本机提取证据").click()
            page.wait_for_url("**/cases/*", timeout=120000)
            page.wait_for_load_state("networkidle")
            content = page.content()
            assert "[PHONE_1]" in content
            assert "13800138000" not in content
            assert "ev_001" in content
            assert "CRITICAL" in content
            assert not errors, errors
            screenshot.parent.mkdir(parents=True, exist_ok=True)
            page.screenshot(path=str(screenshot), full_page=True)
            print(f"PASS {page.url} screenshot={screenshot}")
        finally:
            browser.close()


if __name__ == "__main__":
    main()
