package com.bienestaranimal.zipaquira.evidence

sealed class EvidenceOutcome {
    data class Uploaded(
        val caseId: String,
        val storagePath: String,
        val downloadUrl: String,
    ) : EvidenceOutcome()

    data class Error(val message: String) : EvidenceOutcome()
}
