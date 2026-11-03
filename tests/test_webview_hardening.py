import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

class WebViewHardeningTest(unittest.TestCase):
    def test_main_activity_uses_secure_asset_origin(self):
        main = (ROOT / 'app/src/main/java/com/bienestaranimal/zipaquira/MainActivity.kt').read_text(encoding='utf-8')
        self.assertIn('WebViewAssetLoader.Builder()', main)
        self.assertIn('AssetsPathHandler(this)', main)
        self.assertIn('allowFileAccess = false', main)
        self.assertIn('allowContentAccess = false', main)
        self.assertIn('AppWebViewClient(assetLoader)', main)
        self.assertIn('AppNavigationPolicy.ROOT_URL', main)
        self.assertNotIn('file:///android_asset/index.html', main)

    def test_webview_client_blocks_untrusted_navigation(self):
        path = ROOT / 'app/src/main/java/com/bienestaranimal/zipaquira/web/AppWebViewClient.kt'
        self.assertTrue(path.exists(), 'AppWebViewClient.kt is missing')
        source = path.read_text(encoding='utf-8')
        self.assertIn('assetLoader.shouldInterceptRequest', source)
        self.assertIn('AppNavigationPolicy.isTrusted', source)
        self.assertIn('blockedResponse()', source)
        self.assertIn('if (!AppNavigationPolicy.isTrusted(request.url.toString()))', source)

if __name__ == '__main__':
    unittest.main()
