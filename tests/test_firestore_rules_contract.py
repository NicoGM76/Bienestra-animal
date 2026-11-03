import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class FirestoreRulesContractTest(unittest.TestCase):
    def test_citizen_profile_rules_are_restricted(self):
        rules_path = ROOT / "firestore.rules"
        firebase_json = ROOT / "firebase.json"
        setup_doc = ROOT / "docs/FIREBASE_DEMO_SETUP.md"

        self.assertTrue(rules_path.exists())
        self.assertTrue(firebase_json.exists())
        self.assertTrue(setup_doc.exists())

        rules = rules_path.read_text(encoding="utf-8")
        self.assertIn('rules_version = \'2\';', rules)
        self.assertIn('match /users/{uid}', rules)
        self.assertIn('request.auth.uid == uid', rules)
        self.assertIn('data.roles == ["citizen"]', rules)
        self.assertIn('data.activeRole == "citizen"', rules)
        self.assertIn('data.accountType == "citizen"', rules)
        self.assertIn('data.status == "active"', rules)
        self.assertIn('allow delete: if false;', rules)
        self.assertIn('match /{document=**}', rules)
        self.assertIn('allow read, write: if false;', rules)

        config = firebase_json.read_text(encoding="utf-8")
        self.assertIn('"rules": "firestore.rules"', config)

        setup = setup_doc.read_text(encoding="utf-8")
        for marker in ("demoDebug", "Android 7", "Firebase Authentication", "Cloud Firestore", "users/{uid}"):
            self.assertIn(marker, setup)


if __name__ == "__main__":
    unittest.main()
