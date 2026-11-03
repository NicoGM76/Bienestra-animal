import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class CaseFrontendContractTest(unittest.TestCase):
    def test_report_actions_are_real_navigation_not_pending(self):
        html = (ROOT / 'app/src/main/assets/index.html').read_text(encoding='utf-8')
        js = (ROOT / 'app/src/main/assets/js/app.js').read_text(encoding='utf-8')
        for marker in [
            'data-report-type="lost"', 'data-report-type="found"', 'data-report-type="abuse"',
            'data-screen="report"', 'id="reportDescription"', 'id="reportPriority"',
            'id="reportAnimalCondition"', 'id="reportReferencePoint"', 'id="caseList"'
        ]:
            self.assertIn(marker, html)
        self.assertIn('Android.saveCase(JSON.stringify(payload))', js)
        self.assertIn('Android.loadMyCases()', js)
        self.assertIn("eventName === 'cases:saved'", js)
        self.assertIn("eventName === 'cases:list'", js)

    def test_adoption_is_explicitly_demo(self):
        html = (ROOT / 'app/src/main/assets/index.html').read_text(encoding='utf-8')
        self.assertIn('data-screen="adoptions"', html)
        self.assertIn('CATÁLOGO DEMO', html)
        self.assertIn('Datos ficticios para demostración', html)


if __name__ == '__main__':
    unittest.main()
