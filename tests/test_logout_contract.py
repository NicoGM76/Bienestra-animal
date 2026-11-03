import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

class LogoutContractTest(unittest.TestCase):
    def test_logout_is_wired_through_auth_gateway_and_frontend(self):
        gateway = (ROOT / 'app/src/main/java/com/bienestaranimal/zipaquira/auth/AuthGateway.kt').read_text(encoding='utf-8')
        firebase = (ROOT / 'app/src/main/java/com/bienestaranimal/zipaquira/auth/FirebaseAuthGateway.kt').read_text(encoding='utf-8')
        bridge = (ROOT / 'app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt').read_text(encoding='utf-8')
        html = (ROOT / 'app/src/main/assets/index.html').read_text(encoding='utf-8')
        js = (ROOT / 'app/src/main/assets/js/app.js').read_text(encoding='utf-8')
        self.assertIn('fun signOut()', gateway)
        self.assertIn('auth.signOut()', firebase)
        self.assertIn('fun logout()', bridge)
        self.assertIn('"auth:signedout"', bridge)
        self.assertIn('id="logoutButton"', html)
        self.assertIn('Android.logout()', js)
        self.assertIn("eventName === 'auth:signedout'", js)

if __name__ == '__main__':
    unittest.main()
