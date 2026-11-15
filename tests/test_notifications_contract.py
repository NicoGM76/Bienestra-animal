import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class NotificationsContractTest(unittest.TestCase):
    def test_case_events_generate_role_scoped_notifications(self):
        gateway = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/notifications/NotificationGateway.kt").read_text(encoding="utf-8")
        firebase_notifications = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/notifications/FirebaseNotificationGateway.kt").read_text(encoding="utf-8")
        admin = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/admin/FirebaseAdminGateway.kt").read_text(encoding="utf-8")
        cases = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/cases/FirebaseCaseGateway.kt").read_text(encoding="utf-8")
        bridge = (ROOT / "app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt").read_text(encoding="utf-8")
        html = (ROOT / "app/src/main/assets/index.html").read_text(encoding="utf-8")
        js = (ROOT / "app/src/main/assets/js/app.js").read_text(encoding="utf-8")
        rules = (ROOT / "firestore.rules").read_text(encoding="utf-8")

        self.assertIn("fun listMine", gateway)
        self.assertIn("fun markRead", gateway)
        self.assertIn('collection("notifications")', firebase_notifications)
        self.assertIn('"case_assigned"', admin)
        self.assertIn('"case_completed"', cases)
        self.assertIn("fun loadNotifications", bridge)
        self.assertIn('id="citizenNotificationList"', html)
        self.assertIn('id="vetNotificationList"', html)
        self.assertIn("renderNotifications", js)
        self.assertIn("match /notifications/{notificationId}", rules)


if __name__ == "__main__":
    unittest.main()
