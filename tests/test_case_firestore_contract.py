import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class CaseFirestoreContractTest(unittest.TestCase):
    def test_case_gateway_and_rules_exist(self):
        gateway = ROOT / 'app/src/main/java/com/bienestaranimal/zipaquira/cases/FirebaseCaseGateway.kt'
        self.assertTrue(gateway.exists(), 'FirebaseCaseGateway.kt missing')
        text = gateway.read_text(encoding='utf-8')
        self.assertIn('collection("cases")', text)
        self.assertIn('"createdBy" to user.uid', text)
        self.assertIn('"protectedIdentity" to isProtected', text)
        self.assertIn('"finalPriority" to finalPriority', text)
        self.assertIn('whereEqualTo("createdBy", user.uid)', text)

        rules = (ROOT / 'firestore.rules').read_text(encoding='utf-8')
        self.assertIn('match /cases/{caseId}', rules)
        self.assertIn('request.resource.data.createdBy == request.auth.uid', rules)
        self.assertIn('resource.data.createdBy == request.auth.uid', rules)
        self.assertIn('request.resource.data.status == "received"', rules)
        self.assertIn('request.resource.data.type in ["lost", "found", "abuse"]', rules)
        self.assertIn('request.resource.data.protectedIdentity == (request.resource.data.type == "abuse")', rules)


if __name__ == '__main__':
    unittest.main()
