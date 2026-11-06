import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class AdminInsightsContractTest(unittest.TestCase):
    def test_admin_dashboard_exposes_role_and_case_breakdown(self):
        outcome = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/admin/AdminOutcome.kt").read_text(encoding="utf-8")
        gateway = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/admin/FirebaseAdminGateway.kt").read_text(encoding="utf-8")
        bridge = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt").read_text(encoding="utf-8")
        html = (ROOT / "app/src/main/assets/index.html").read_text(encoding="utf-8")
        js = (ROOT / "app/src/main/assets/js/app.js").read_text(encoding="utf-8")

        for marker in [
            "citizenCount",
            "veterinarianCount",
            "administratorCount",
            "receivedCaseCount",
            "assignedCaseCount",
            "inProgressCaseCount",
            "completedCaseCount",
        ]:
            self.assertIn(marker, outcome)
            self.assertIn(marker, gateway)
            self.assertIn(marker, bridge)

        self.assertIn('id="adminRoleMetrics"', html)
        self.assertIn('id="adminCaseMetrics"', html)
        self.assertIn("renderAdminDashboard", js)
        self.assertIn("Casos en atención", html)


if __name__ == "__main__":
    unittest.main()
