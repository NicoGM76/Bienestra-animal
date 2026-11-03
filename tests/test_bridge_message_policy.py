import subprocess
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'app/src/main/java/com/bienestaranimal/zipaquira/web/BridgeMessagePolicy.kt'

RUNNER = r'''
import com.bienestaranimal.zipaquira.web.BridgeMessagePolicy

fun check(value: Boolean, message: String) {
    if (!value) throw IllegalStateException(message)
}

fun main() {
    check(BridgeMessagePolicy.sanitize("  Hola  ") == "Hola", "trim failed")
    check(BridgeMessagePolicy.sanitize("   ") == null, "blank message accepted")
    check(BridgeMessagePolicy.sanitize("") == null, "empty message accepted")
    check(BridgeMessagePolicy.sanitize("a".repeat(200))?.length == 200, "200-char message rejected")
    check(BridgeMessagePolicy.sanitize("a".repeat(201)) == null, "oversized message accepted")
}
'''

class BridgeMessagePolicyJvmTest(unittest.TestCase):
    def test_bridge_message_policy_contract(self):
        self.assertTrue(SOURCE.exists(), f'Missing {SOURCE}')
        with tempfile.TemporaryDirectory() as td:
            td = Path(td)
            runner = td / 'Runner.kt'
            jar = td / 'bridge-test.jar'
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
