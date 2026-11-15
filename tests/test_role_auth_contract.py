import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class RoleAuthContractTest(unittest.TestCase):
    def test_role_aware_auth_contract(self):
        outcome = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/auth/AuthOutcome.kt").read_text(encoding="utf-8")
        gateway = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/auth/FirebaseAuthGateway.kt").read_text(encoding="utf-8")
        bridge = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt").read_text(encoding="utf-8")

        for marker in ("val name: String", "val activeRole: String", "val roles: List<String>"):
            self.assertIn(marker, outcome)

        self.assertIn('firestore.collection("users").document(user.uid)', gateway)
        self.assertIn('status != "active"', gateway)
        self.assertIn('activeRole !in roles', gateway)
        for role in ('"citizen"', '"veterinarian"', '"administrator"'):
            self.assertIn(role, gateway)

        self.assertIn('.put("activeRole", outcome.activeRole)', bridge)
        self.assertIn('.put("roles", JSONArray(outcome.roles))', bridge)
        self.assertGreaterEqual(bridge.count('.put("activeRole", outcome.activeRole)'), 2)
        self.assertGreaterEqual(bridge.count('.put("roles", JSONArray(outcome.roles))'), 2)


if __name__ == "__main__":
    unittest.main()
