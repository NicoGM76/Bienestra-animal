package com.bienestaranimal.zipaquira.notifications

interface NotificationGateway {
    fun listMine(callback: (NotificationOutcome) -> Unit)
    fun markRead(notificationId: String, callback: (NotificationOutcome) -> Unit)
}
