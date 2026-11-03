import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class RoleFrontendContractTest(unittest.TestCase):
    def test_role_routing_and_no_map_promise(self):
        js = (ROOT / "app/src/main/assets/js/app.js").read_text(encoding="utf-8")
        html = (ROOT / "app/src/main/assets/index.html").read_text(encoding="utf-8")

        self.assertIn("openPortalForRole", js)
        for role, screen in (
            ("citizen", "home"),
            ("veterinarian", "vet-home"),
            ("administrator", "admin-home"),
        ):
            self.assertIn(role, js)
            self.assertIn(screen, js)
            self.assertIn(f'data-screen="{screen}"', html)

        self.assertNotIn('id="mapButton"', html)
        self.assertNotIn("Confirmar en mapa", html)
        self.assertNotIn("Mapa pendiente", js)
        self.assertIn("currentRole", js)
        self.assertIn("protectedScreenRoles", js)
        self.assertIn("clearSessionData", js)
        self.assertIn("history.replaceState", js)


if __name__ == "__main__":
    unittest.main()
