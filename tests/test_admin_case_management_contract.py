import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class AdminCaseManagementContractTest(unittest.TestCase):
    def test_admin_has_filters_detail_priority_and_reassignment(self):
        outcome = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/admin/AdminOutcome.kt").read_text(encoding="utf-8")
        gateway = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/admin/AdminGateway.kt").read_text(encoding="utf-8")
        firebase = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/admin/FirebaseAdminGateway.kt").read_text(encoding="utf-8")
        bridge = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt").read_text(encoding="utf-8")
        html = (ROOT / "app/src/main/assets/index.html").read_text(encoding="utf-8")
        js = (ROOT / "app/src/main/assets/js/app.js").read_text(encoding="utf-8")
        rules = (ROOT / "firestore.rules").read_text(encoding="utf-8")

        self.assertIn("aiSuggestedPriority", outcome)
        self.assertIn("fun reassignCase", gateway)
        self.assertIn("fun updateCasePriority", gateway)
        self.assertIn("priorityReviewedBy", firebase)
        self.assertIn("case_reassigned", firebase)
        self.assertIn("fun updateAdminCasePriority", bridge)
        self.assertIn("fun reassignCaseToVeterinarian", bridge)
        self.assertIn('data-screen="admin-case"', html)
        self.assertIn('id="adminCaseSearch"', html)
        self.assertIn('id="adminStatusFilter"', html)
        self.assertIn("renderAdminCaseManagement", js)
        self.assertIn("openAdminCase", js)
        self.assertIn("priorityReviewedAt", rules)


if __name__ == "__main__":
    unittest.main()
