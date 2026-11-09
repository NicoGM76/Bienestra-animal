import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class ClinicalHistoryContractTest(unittest.TestCase):
    def test_each_veterinary_save_creates_history_entry(self):
        model = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/cases/ClinicalRecordSummary.kt").read_text(encoding="utf-8")
        gateway = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/cases/CaseGateway.kt").read_text(encoding="utf-8")
        firebase = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/cases/FirebaseCaseGateway.kt").read_text(encoding="utf-8")
        bridge = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt").read_text(encoding="utf-8")
        html = (ROOT / "app/src/main/assets/index.html").read_text(encoding="utf-8")
        js = (ROOT / "app/src/main/assets/js/app.js").read_text(encoding="utf-8")
        rules = (ROOT / "firestore.rules").read_text(encoding="utf-8")

        self.assertIn("data class ClinicalRecordSummary", model)
        self.assertIn("fun listClinicalHistory", gateway)
        self.assertIn('collection("attentions")', firebase)
        self.assertIn('"legacy_case_snapshot"', firebase)
        self.assertIn("fun loadClinicalHistory", bridge)
        self.assertIn('id="vetClinicalHistory"', html)
        self.assertIn("renderClinicalHistory", js)
        self.assertIn("match /attentions/{attentionId}", rules)
        self.assertIn("allow update, delete: if false", rules)


if __name__ == "__main__":
    unittest.main()
