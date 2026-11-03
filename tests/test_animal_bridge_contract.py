import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

class AnimalBridgeContractTest(unittest.TestCase):
    def test_bridge_exposes_animal_actions_without_owner_uid_parameter(self):
        bridge = (ROOT / 'app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt').read_text(encoding='utf-8')
        main = (ROOT / 'app/src/main/java/com/bienestaranimal/zipaquira/MainActivity.kt').read_text(encoding='utf-8')
        self.assertIn('private val animalGateway: AnimalGateway', bridge)
        self.assertIn('fun saveAnimal(payloadJson: String)', bridge)
        self.assertIn('fun loadMyAnimals()', bridge)
        self.assertNotIn('fun saveAnimal(uid:', bridge)
        self.assertIn('"animals:loading"', bridge)
        self.assertIn('"animals:saved"', bridge)
        self.assertIn('"animals:list"', bridge)
        self.assertIn('"animals:error"', bridge)
        self.assertIn('FirebaseAnimalGateway()', main)
        self.assertIn('WebAppInterface(this, authGateway, animalGateway, caseGateway, eventSink)', main)

if __name__ == '__main__':
    unittest.main()
