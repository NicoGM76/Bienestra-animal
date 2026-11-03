from pathlib import Path
import unittest

APP_JS = Path(__file__).parents[1] / "app" / "src" / "main" / "assets" / "js" / "app.js"

class WebViewCompatibilityTests(unittest.TestCase):
    def test_javascript_avoids_optional_chaining_for_legacy_webview(self):
        source = APP_JS.read_text(encoding="utf-8")
        self.assertNotIn("?.", source, "Optional chaining requires Chromium/WebView 80+")

if __name__ == "__main__":
    unittest.main()
