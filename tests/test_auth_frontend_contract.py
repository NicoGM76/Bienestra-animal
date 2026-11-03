import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class AuthFrontendContractTest(unittest.TestCase):
    def test_real_auth_ui_contract(self):
        html = (ROOT / "app/src/main/assets/index.html").read_text(encoding="utf-8")
        js = (ROOT / "app/src/main/assets/js/app.js").read_text(encoding="utf-8")

        self.assertNotIn("autenticación simulada", html.lower())
        self.assertNotIn("Ingreso simulado", js)
        self.assertIn('data-screen="signup"', html)
        self.assertIn('id="signupName"', html)
        self.assertIn('id="signupEmail"', html)
        self.assertIn('id="signupPassword"', html)
        self.assertIn('id="signupButton"', html)
        self.assertIn('id="authMessage"', html)
        self.assertIn('id="signupMessage"', html)

        self.assertIn("Android.login(email, password)", js)
        self.assertIn("Android.registerCitizen(name, email, password)", js)
        self.assertIn("window.BAZNative", js)
        self.assertIn("onNativeEvent", js)
        self.assertIn("auth:loading", js)
        self.assertIn("auth:success", js)
        self.assertIn("auth:registered", js)
        self.assertIn("auth:error", js)
        self.assertNotIn("go('home');\n  });", js)
        self.assertIn("loginButton.disabled", js)
        self.assertIn("signupButton.disabled", js)
        self.assertNotIn('data-go="home">Continuar como invitado', html)
        self.assertNotIn('<strong>Luna</strong>', html)
        self.assertNotIn('<strong>Milo</strong>', html)
        self.assertIn('id="animalList"', html)
        self.assertNotIn('<strong>Luna</strong>', html)
        self.assertNotIn('<strong>Milo</strong>', html)


if __name__ == "__main__":
    unittest.main()
