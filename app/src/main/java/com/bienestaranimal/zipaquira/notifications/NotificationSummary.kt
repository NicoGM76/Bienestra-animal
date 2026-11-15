package com.bienestaranimal.zipaquira.notifications

data class NotificationSummary(
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    val caseId: String,
    val read: Boolean,
    val createdAtMillis: Long,
)
