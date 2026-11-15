import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class RoleFirestoreRulesContractTest(unittest.TestCase):
    def test_role_based_rules_are_least_privilege(self):
        rules = (ROOT / "firestore.rules").read_text(encoding="utf-8")

        for marker in (
            "function isCitizen()",
            "function isVeterinarian()",
            "function isAdministrator()",
            'resource.data.assignedTo == request.auth.uid',
            'affectedKeys().hasOnly(["status", "updatedAt"])',
            'resource.data.status == "assigned"',
            'request.resource.data.status == "in_progress"',
            'resource.data.status == "in_progress"',
            'request.resource.data.status == "completed"',
        ):
            self.assertIn(marker, rules)

        self.assertIn("allow delete: if false;", rules)
        self.assertIn("allow read, write: if false;", rules)


if __name__ == "__main__":
    unittest.main()
