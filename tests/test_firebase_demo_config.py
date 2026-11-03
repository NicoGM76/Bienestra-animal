import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class FirebaseDemoConfigTest(unittest.TestCase):
    def test_firebase_is_wired_only_for_demo(self):
        root_gradle = (ROOT / "build.gradle.kts").read_text(encoding="utf-8")
        app_gradle = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
        demo_json = ROOT / "app/src/demo/google-services.json"
        prod_json = ROOT / "app/src/prod/google-services.json"

        self.assertIn('id("com.google.gms.google-services") version "4.5.0" apply false', root_gradle)
        self.assertIn('id("com.google.gms.google-services")', app_gradle)
        self.assertIn('platform("com.google.firebase:firebase-bom:34.11.0")', app_gradle)
        self.assertIn('implementation("com.google.firebase:firebase-auth")', app_gradle)
        self.assertIn('implementation("com.google.firebase:firebase-firestore")', app_gradle)
        self.assertTrue(demo_json.exists(), "demo google-services.json is required")
        self.assertFalse(prod_json.exists(), "production must not reuse demo Firebase config")

        config = json.loads(demo_json.read_text(encoding="utf-8"))
        package_name = config["client"][0]["client_info"]["android_client_info"]["package_name"]
        self.assertEqual("com.bienestaranimal.zipaquira.demo", package_name)


if __name__ == "__main__":
    unittest.main()
