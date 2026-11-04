import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class FirebaseDemoConfigTest(unittest.TestCase):
    def test_firebase_is_wired_only_for_demo(self):
        root_gradle = (ROOT / "build.gradle.kts").read_text(encoding="utf-8")
        app_gradle = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
        gitignore = (ROOT / ".gitignore").read_text(encoding="utf-8")
        setup = (ROOT / "docs/FIREBASE_DEMO_SETUP.md").read_text(encoding="utf-8")
        prod_json = ROOT / "app/src/prod/google-services.json"

        self.assertIn('id("com.google.gms.google-services") version "4.5.0" apply false', root_gradle)
        self.assertIn('id("com.google.gms.google-services")', app_gradle)
        self.assertIn('platform("com.google.firebase:firebase-bom:34.11.0")', app_gradle)
        self.assertIn('implementation("com.google.firebase:firebase-auth")', app_gradle)
        self.assertIn('implementation("com.google.firebase:firebase-firestore")', app_gradle)

        self.assertIn("google-services.json", gitignore)
        self.assertIn("app/src/demo/google-services.json", setup)
        self.assertIn("com.bienestaranimal.zipaquira.demo", setup)
        self.assertFalse(prod_json.exists(), "production must not reuse demo Firebase config")


if __name__ == "__main__":
    unittest.main()
