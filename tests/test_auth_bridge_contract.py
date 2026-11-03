import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class AuthBridgeContractTest(unittest.TestCase):
    def test_auth_gateway_and_bridge_contract(self):
        auth_gateway = ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/auth/AuthGateway.kt"
        firebase_gateway = ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/auth/FirebaseAuthGateway.kt"
        web_event_sink = ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/web/WebEventSink.kt"
        webview_event_sink = ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/web/WebViewEventSink.kt"
        bridge = ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt"

        for path in (auth_gateway, firebase_gateway, web_event_sink, webview_event_sink, bridge):
            self.assertTrue(path.exists(), f"Missing {path}")

        gateway_text = auth_gateway.read_text(encoding="utf-8")
        self.assertIn("fun signIn(email: String, password: String, callback: (AuthOutcome) -> Unit)", gateway_text)
        self.assertIn("fun registerCitizen(name: String, email: String, password: String, callback: (AuthOutcome) -> Unit)", gateway_text)

        firebase_text = firebase_gateway.read_text(encoding="utf-8")
        self.assertIn('collection("users").document(user.uid)', firebase_text)
        for required in ('"roles"', '"activeRole"', '"accountType"', '"status"'):
            self.assertIn(required, firebase_text)
        self.assertNotIn("exception.message", firebase_text)

        bridge_text = bridge.read_text(encoding="utf-8")
        self.assertRegex(bridge_text, r'fun\s+login\(email:\s*String,\s*password:\s*String\)')
        self.assertRegex(bridge_text, r'fun\s+registerCitizen\(name:\s*String,\s*email:\s*String,\s*password:\s*String\)')
        self.assertNotRegex(bridge_text, r'fun\s+(setRole|assignRole|registerCitizen)\([^)]*role')
        self.assertIn('"auth:loading"', bridge_text)
        self.assertIn('"auth:success"', bridge_text)
        self.assertIn('"auth:registered"', bridge_text)
        self.assertIn('"auth:error"', bridge_text)

    def test_main_activity_injects_auth_gateway(self):
        text = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/MainActivity.kt").read_text(encoding="utf-8")
        self.assertIn("FirebaseAuthGateway", text)
        self.assertIn("WebViewEventSink", text)
        self.assertIn("WebAppInterface(", text)


if __name__ == "__main__":
    unittest.main()
