import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class LegacyCaseCompatibilityContractTest(unittest.TestCase):
    def test_legacy_cases_are_normalized_without_manual_database_edits(self):
        compatibility = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/cases/CaseCompatibility.kt").read_text(encoding="utf-8")
        case_gateway = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/cases/FirebaseCaseGateway.kt").read_text(encoding="utf-8")
        admin_gateway = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/admin/FirebaseAdminGateway.kt").read_text(encoding="utf-8")
        rules = (ROOT / "firestore.rules").read_text(encoding="utf-8")

        self.assertIn("object CaseCompatibility", compatibility)
        self.assertIn('"recibido"', compatibility)
        self.assertIn('"asignado"', compatibility)
        self.assertIn('"realizado"', compatibility)
        self.assertIn('"neighborhood"', case_gateway)
        self.assertIn('"location"', case_gateway)
        self.assertIn("CaseCompatibility::status", admin_gateway)
        self.assertIn("caseWasReceived", rules)


if __name__ == "__main__":
    unittest.main()
