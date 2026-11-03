import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

class ProjectConfigTest(unittest.TestCase):
    def test_android_compatibility_and_flavors_are_configured(self):
        gradle = (ROOT / 'app/build.gradle.kts').read_text(encoding='utf-8')
        self.assertIn('minSdk = 23', gradle)
        self.assertIn('flavorDimensions += "environment"', gradle)
        self.assertIn('create("demo")', gradle)
        self.assertIn('create("prod")', gradle)
        self.assertIn('BuildConfig.ENVIRONMENT', (ROOT / 'README.md').read_text(encoding='utf-8'))
        self.assertIn('androidx.webkit:webkit:1.15.0', gradle)
        self.assertIn('testImplementation("junit:junit:4.13.2")', gradle)

    def test_environment_string_resources_exist(self):
        expected = {
            ROOT / 'app/src/main/res/values/strings.xml': 'Bienestar Animal Zipaquirá',
            ROOT / 'app/src/demo/res/values/strings.xml': 'DEMO',
            ROOT / 'app/src/prod/res/values/strings.xml': 'Producción',
        }
        for path, marker in expected.items():
            self.assertTrue(path.exists(), f'Missing {path}')
            self.assertIn(marker, path.read_text(encoding='utf-8'))

if __name__ == '__main__':
    unittest.main()
