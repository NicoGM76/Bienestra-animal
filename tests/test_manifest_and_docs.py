import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

class ManifestAndDocsTest(unittest.TestCase):
    def test_manifest_declares_internet_for_firebase(self):
        manifest = (ROOT / 'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
        self.assertIn('android.permission.INTERNET', manifest)

    def test_manifest_disables_cleartext_and_uses_resource_label(self):
        manifest = (ROOT / 'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
        self.assertIn('android:usesCleartextTraffic="false"', manifest)
        self.assertIn('android:networkSecurityConfig="@xml/network_security_config"', manifest)
        self.assertIn('android:label="@string/app_name"', manifest)

    def test_network_security_config_denies_cleartext(self):
        path = ROOT / 'app/src/main/res/xml/network_security_config.xml'
        self.assertTrue(path.exists(), 'network_security_config.xml is missing')
        source = path.read_text(encoding='utf-8')
        self.assertIn('cleartextTrafficPermitted="false"', source)

    def test_compatibility_document_exists(self):
        path = ROOT / 'docs/COMPATIBILIDAD_ANDROID.md'
        self.assertTrue(path.exists(), 'compatibility documentation is missing')
        text = path.read_text(encoding='utf-8')
        for marker in ['API 23', 'API 24', 'API 35', 'API 36', 'red lenta', 'almacenamiento']:
            self.assertIn(marker, text)

if __name__ == '__main__':
    unittest.main()
