import subprocess
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'app/src/main/java/com/bienestaranimal/zipaquira/web/AppNavigationPolicy.kt'

RUNNER = r'''
import com.bienestaranimal.zipaquira.web.AppNavigationPolicy

fun check(value: Boolean, message: String) {
    if (!value) throw IllegalStateException(message)
}

fun main() {
    check(AppNavigationPolicy.isTrusted("https://appassets.androidplatform.net/assets/index.html"), "trusted root rejected")
    check(AppNavigationPolicy.isTrusted("https://appassets.androidplatform.net/assets/css/styles.css"), "trusted asset rejected")
    check(!AppNavigationPolicy.isTrusted("file:///android_asset/index.html"), "file URL accepted")
    check(!AppNavigationPolicy.isTrusted("http://appassets.androidplatform.net/assets/index.html"), "http accepted")
    check(!AppNavigationPolicy.isTrusted("https://example.com/assets/index.html"), "external host accepted")
    check(!AppNavigationPolicy.isTrusted("https://appassets.androidplatform.net/other/index.html"), "outside asset path accepted")
    check(!AppNavigationPolicy.isTrusted("javascript:alert(1)"), "javascript scheme accepted")
    check(!AppNavigationPolicy.isTrusted("not a url"), "invalid URL accepted")
}
'''

class NavigationPolicyJvmTest(unittest.TestCase):
    def test_navigation_policy_contract(self):
        self.assertTrue(SOURCE.exists(), f'Missing {SOURCE}')
        with tempfile.TemporaryDirectory() as td:
            td = Path(td)
            runner = td / 'Runner.kt'
            jar = td / 'navigation-test.jar'
            runner.write_text(RUNNER, encoding='utf-8')
            compile_result = subprocess.run(
                ['kotlinc', str(SOURCE), str(runner), '-include-runtime', '-d', str(jar)],
                capture_output=True, text=True
            )
            self.assertEqual(0, compile_result.returncode, compile_result.stderr)
            run_result = subprocess.run(['java', '-jar', str(jar)], capture_output=True, text=True)
            self.assertEqual(0, run_result.returncode, run_result.stderr)

if __name__ == '__main__':
    unittest.main()
