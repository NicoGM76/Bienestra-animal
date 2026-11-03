import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class VeterinarianCaseContractTest(unittest.TestCase):
    def test_assigned_case_workflow(self):
        gateway = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/cases/CaseGateway.kt").read_text(encoding="utf-8")
        firebase = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/cases/FirebaseCaseGateway.kt").read_text(encoding="utf-8")
        summary = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/cases/CaseSummary.kt").read_text(encoding="utf-8")
        bridge = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt").read_text(encoding="utf-8")
        js = (ROOT / "app/src/main/assets/js/app.js").read_text(encoding="utf-8")

        self.assertIn("listAssignedCases", gateway)
        self.assertIn("updateAssignedCaseStatus", gateway)
        self.assertIn('whereEqualTo("assignedTo", user.uid)', firebase)
        self.assertIn('"assigned" to "in_progress"', firebase)
        self.assertIn('"in_progress" to "completed"', firebase)
        self.assertIn("addressOrLocation", summary)
        self.assertIn("loadAssignedCases", bridge)
        self.assertIn("updateAssignedCaseStatus", bridge)
        self.assertIn("vet:cases", bridge)
        self.assertIn("requestVeterinarianCases", js)
        self.assertIn("renderVeterinarianCases", js)


if __name__ == "__main__":
    unittest.main()
