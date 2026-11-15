import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class VeterinarianCareContractTest(unittest.TestCase):
    def test_veterinarian_clinical_care_contract(self):
        gateway = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/cases/CaseGateway.kt").read_text(encoding="utf-8")
        firebase = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/cases/FirebaseCaseGateway.kt").read_text(encoding="utf-8")
        bridge = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt").read_text(encoding="utf-8")
        html = (ROOT / "app/src/main/assets/index.html").read_text(encoding="utf-8")
        js = (ROOT / "app/src/main/assets/js/app.js").read_text(encoding="utf-8")
        rules = (ROOT / "firestore.rules").read_text(encoding="utf-8")

        self.assertIn("saveVeterinaryCare", gateway)
        self.assertIn('"veterinaryDiagnosis"', firebase)
        self.assertIn('"veterinaryObservations"', firebase)
        self.assertIn('"veterinaryTreatment"', firebase)
        self.assertIn('"veterinaryRecommendations"', firebase)
        self.assertIn("fun saveVeterinaryCare", bridge)
        self.assertIn('data-screen="vet-care"', html)
        self.assertIn("vetDiagnosis", html)
        self.assertIn("vetTreatment", html)
        self.assertIn("vetRecommendations", html)
        self.assertIn("vet:care-saved", js)
        self.assertIn("request.resource.data.diff(resource.data).affectedKeys().hasOnly", rules)
        self.assertIn('"veterinaryDiagnosis"', rules)
        self.assertIn('"veterinaryTreatment"', rules)

    def test_veterinarian_dashboard_has_workload_metrics(self):
        html = (ROOT / "app/src/main/assets/index.html").read_text(encoding="utf-8")
        js = (ROOT / "app/src/main/assets/js/app.js").read_text(encoding="utf-8")

        self.assertIn('id="vetMetrics"', html)
        self.assertIn("renderVeterinarianMetrics", js)


if __name__ == "__main__":
    unittest.main()
