package com.bienestaranimal.zipaquira.web

/** Validation for short text crossing the JavaScript → Kotlin bridge. */
object BridgeMessagePolicy {
    const val MAX_MESSAGE_LENGTH = 200

    fun sanitize(message: String): String? {
        val normalized = message.trim()
        if (normalized.isEmpty()) return null
        if (normalized.length > MAX_MESSAGE_LENGTH) return null
        return normalized
    }
}
