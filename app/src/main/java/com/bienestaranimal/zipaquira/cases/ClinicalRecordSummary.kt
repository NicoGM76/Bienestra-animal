package com.bienestaranimal.zipaquira.cases

data class ClinicalRecordSummary(
    val id: String,
    val diagnosis: String,
    val observations: String,
    val treatment: String,
    val recommendations: String,
    val veterinarianUid: String,
    val createdAtMillis: Long,
    val source: String,
)
