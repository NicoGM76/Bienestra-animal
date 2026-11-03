import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

class AnimalFirestoreContractTest(unittest.TestCase):
    def test_gateway_uses_authenticated_owner_and_animals_collection(self):
        gateway = (ROOT / 'app/src/main/java/com/bienestaranimal/zipaquira/animals/FirebaseAnimalGateway.kt').read_text(encoding='utf-8')
        self.assertIn('FirebaseAuth.getInstance()', gateway)
        self.assertIn('currentUser', gateway)
        self.assertIn('collection("animals")', gateway)
        self.assertIn('"mainResponsibleId" to user.uid', gateway)
        self.assertIn('"createdBy" to user.uid', gateway)
        self.assertIn('FieldValue.serverTimestamp()', gateway)
        self.assertIn('whereEqualTo("mainResponsibleId", user.uid)', gateway)

    def test_rules_restrict_animals_to_the_authenticated_main_responsible(self):
        rules = (ROOT / 'firestore.rules').read_text(encoding='utf-8')
        self.assertIn('match /animals/{animalId}', rules)
        self.assertIn('request.resource.data.mainResponsibleId == request.auth.uid', rules)
        self.assertIn('resource.data.mainResponsibleId == request.auth.uid', rules)
        self.assertIn('allow update: if false;', rules)
        self.assertIn('allow delete: if false;', rules)

if __name__ == '__main__':
    unittest.main()
