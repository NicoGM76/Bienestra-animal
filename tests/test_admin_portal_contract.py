import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class AdminPortalContractTest(unittest.TestCase):
    def test_read_only_admin_dashboard_contract(self):
        gateway = ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/admin/AdminGateway.kt"
        firebase = ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/admin/FirebaseAdminGateway.kt"
        outcome = ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/admin/AdminOutcome.kt"
        bridge = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt").read_text(encoding="utf-8")
        js = (ROOT / "app/src/main/assets/js/app.js").read_text(encoding="utf-8")
        html = (ROOT / "app/src/main/assets/index.html").read_text(encoding="utf-8")

        self.assertTrue(gateway.exists())
        self.assertTrue(firebase.exists())
        self.assertTrue(outcome.exists())

        gateway_text = gateway.read_text(encoding="utf-8")
        firebase_text = firebase.read_text(encoding="utf-8")

        self.assertIn("loadDashboard", gateway_text)
        self.assertNotIn("save", gateway_text.lower())
        self.assertNotIn("delete", gateway_text.lower())
        self.assertNotIn("update", gateway_text.lower())

        for collection in ('collection("users")', 'collection("animals")', 'collection("cases")'):
            self.assertIn(collection, firebase_text)

        self.assertIn("loadAdminDashboard", bridge)
        self.assertIn("admin:dashboard", bridge)
        self.assertIn("requestAdminDashboard", js)
        self.assertIn("renderAdminDashboard", js)
        self.assertIn('id="adminMetrics"', html)
        self.assertIn('id="adminRecentList"', html)


if __name__ == "__main__":
    unittest.main()
