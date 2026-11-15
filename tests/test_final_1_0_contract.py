import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class FinalOneContractTest(unittest.TestCase):
    def test_android_release_candidate_configuration(self):
        gradle = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
        self.assertIn('versionName = "1.0.0-rc1"', gradle)
        self.assertIn('firebase-storage', gradle)
        self.assertIn('firebase-messaging', gradle)
        self.assertIn('targetSdk = 35', gradle)

    def test_offline_contract_is_explicit(self):
        app = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/BienestarAnimalApp.kt").read_text(encoding="utf-8")
        bridge = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt").read_text(encoding="utf-8")
        html = (ROOT / "app/src/main/assets/index.html").read_text(encoding="utf-8")
        self.assertIn("setPersistenceEnabled(true)", app)
        self.assertIn("CACHE_SIZE_UNLIMITED", app)
        self.assertIn("getConnectivityStatus", bridge)
        self.assertIn("Modo conectado y offline", html)

    def test_dashboard_and_adoption_simulation_exist(self):
        html = (ROOT / "app/src/main/assets/index.html").read_text(encoding="utf-8")
        js = (ROOT / "app/src/main/assets/js/app.js").read_text(encoding="utf-8")
        self.assertIn('data-citizen-segment="services"', html)
        self.assertIn("Reporta en 3 pasos", html)
        self.assertIn("MÓDULO SIMULADO", html)
        self.assertIn("baz-adoption-requests-v1", js)

    def test_animal_edit_is_owner_limited(self):
        gateway = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/animals/FirebaseAnimalGateway.kt").read_text(encoding="utf-8")
        rules = (ROOT / "firestore.rules").read_text(encoding="utf-8")
        self.assertIn("override fun updateAnimal", gateway)
        self.assertIn('resource.data.mainResponsibleId == request.auth.uid', rules)
        self.assertIn('"referencePoint"', rules)

    def test_evidence_storage_pipeline_exists(self):
        gateway = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/evidence/FirebaseEvidenceGateway.kt").read_text(encoding="utf-8")
        rules = (ROOT / "storage.rules").read_text(encoding="utf-8")
        js = (ROOT / "app/src/main/assets/js/app.js").read_text(encoding="utf-8")
        self.assertIn('caseEvidence/', gateway)
        self.assertIn('evidenceUrls', gateway)
        self.assertIn("image/.*", rules)
        self.assertIn("evidence:uploaded", js)

    def test_push_and_audit_backend_are_configured(self):
        firebase = json.loads((ROOT / "firebase.json").read_text(encoding="utf-8"))
        functions = (ROOT / "functions/index.js").read_text(encoding="utf-8")
        manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
        self.assertEqual(firebase["functions"]["source"], "functions")
        self.assertIn("sendPushForNotification", functions)
        self.assertIn("auditCaseWrite", functions)
        self.assertIn("AppMessagingService", manifest)
        self.assertIn("POST_NOTIFICATIONS", manifest)

    def test_api_smoke_matrix_covers_key_versions(self):
        workflow = (ROOT / ".github/workflows/android-api-smoke.yml").read_text(encoding="utf-8")
        self.assertIn("api-level: [26, 33, 35]", workflow)
        self.assertIn("connectedDemoDebugAndroidTest", workflow)


if __name__ == "__main__":
    unittest.main()
