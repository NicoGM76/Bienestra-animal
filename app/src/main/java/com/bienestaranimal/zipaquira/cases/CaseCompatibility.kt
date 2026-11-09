package com.bienestaranimal.zipaquira.cases

import com.google.firebase.firestore.DocumentSnapshot

object CaseCompatibility {
    fun status(document: DocumentSnapshot): String =
        normalizeStatus(
            firstString(document, "status", "caseStatus", "state")
        )

    fun priority(document: DocumentSnapshot): String =
        normalizePriority(
            firstString(document, "finalPriority", "priority", "userPriority")
        )

    fun text(document: DocumentSnapshot, vararg keys: String): String =
        firstString(document, *keys)

    fun count(document: DocumentSnapshot): Int =
        ((document.getLong("animalCount")
            ?: document.getLong("animalsCount")
            ?: 1L)
            .coerceAtLeast(1L))
            .toInt()

    fun isLegacy(document: DocumentSnapshot): Boolean =
        (document.getLong("schemaVersion") ?: 1L) < 2L

    fun normalizeStatus(value: String?): String = when (value.orEmpty().trim().lowercase()) {
        "", "received", "recibido", "nuevo", "new" -> "received"
        "under_review", "under review", "en_revision", "en revisión", "revision" -> "received"
        "assigned", "asignado" -> "assigned"
        "in_progress", "in progress", "en_atencion", "en atención", "atencion" -> "in_progress"
        "completed", "realizado", "finalizado", "cerrado", "closed" -> "completed"
        else -> value.orEmpty().trim().lowercase()
    }

    fun normalizePriority(value: String?): String = when (value.orEmpty().trim().lowercase()) {
        "low", "baja", "bajo" -> "low"
        "medium", "media", "medio" -> "medium"
        "high", "alta", "alto" -> "high"
        "critical", "critica", "crítica", "critico", "crítico" -> "critical"
        "", "system", "pending_review", "pendiente", "pendiente de revisión" -> "pending_review"
        else -> value.orEmpty().trim().lowercase()
    }

    private fun firstString(document: DocumentSnapshot, vararg keys: String): String {
        keys.forEach { key ->
            val value = document.getString(key).orEmpty().trim()
            if (value.isNotEmpty()) return value
        }
        return ""
    }
}
