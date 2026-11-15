package com.bienestaranimal.zipaquira.notifications

sealed class NotificationOutcome {
    data class Listed(val notifications: List<NotificationSummary>) : NotificationOutcome()
    data class Read(val notificationId: String) : NotificationOutcome()
    data class Error(val message: String) : NotificationOutcome()
}
