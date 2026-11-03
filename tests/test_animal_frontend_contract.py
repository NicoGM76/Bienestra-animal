import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

class AnimalFrontendContractTest(unittest.TestCase):
    def test_form_and_list_are_wired_to_native_animal_bridge(self):
        html = (ROOT / 'app/src/main/assets/index.html').read_text(encoding='utf-8')
        js = (ROOT / 'app/src/main/assets/js/app.js').read_text(encoding='utf-8')

        for field_id in [
            'animalName', 'animalBreed', 'animalColor', 'animalSex', 'animalAge',
            'animalSize', 'animalMunicipality', 'animalNeighborhoodOrVillage',
            'animalAddressOrLocation', 'animalReferencePoint', 'animalList', 'animalMessage'
        ]:
            self.assertIn(f'id="{field_id}"', html)

        self.assertIn('data-direction="Vereda/Sector"', html)
        self.assertIn('id="saveAnimal"', html)
        self.assertNotIn('Guardar registro simulado', html)
        self.assertNotIn('guardado de forma simulada', js)
        self.assertIn('Android.saveAnimal(JSON.stringify(payload))', js)
        self.assertIn('Android.loadMyAnimals()', js)
        self.assertIn("eventName === 'animals:list'", js)
        self.assertIn("eventName === 'animals:saved'", js)
        self.assertIn('document.createElement', js)
        self.assertNotIn('.innerHTML', js)

if __name__ == '__main__':
    unittest.main()
