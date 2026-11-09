import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class PriorityAiBetaContractTest(unittest.TestCase):
    def test_ai_beta_is_explainable_and_non_authoritative(self):
        assistant = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/cases/CasePriorityAssistant.kt").read_text(encoding="utf-8")
        case_gateway = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/cases/FirebaseCaseGateway.kt").read_text(encoding="utf-8")
        admin = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/admin/FirebaseAdminGateway.kt").read_text(encoding="utf-8")
        html = (ROOT / "app/src/main/assets/index.html").read_text(encoding="utf-8")
        js = (ROOT / "app/src/main/assets/js/app.js").read_text(encoding="utf-8")

        self.assertIn('version: String = "heuristic-beta-1"', assistant)
        self.assertIn("val reason: String", assistant)
        self.assertIn('"aiSuggestedPriority" to suggestion.priority', case_gateway)
        self.assertIn('"finalPriority" to finalPriority', case_gateway)
        self.assertIn("updateCasePriority", admin)
        self.assertIn("ASISTENTE DE PRIORIDAD · IA BETA", html)
        self.assertIn("La decisión final permanece en el administrador", html)
        self.assertIn("useAiPriorityButton", js)


if __name__ == "__main__":
    unittest.main()
