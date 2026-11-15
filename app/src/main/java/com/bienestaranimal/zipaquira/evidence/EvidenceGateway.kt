package com.bienestaranimal.zipaquira.evidence

import android.net.Uri

interface EvidenceGateway {
    fun uploadCaseImage(caseId: String, uri: Uri, callback: (EvidenceOutcome) -> Unit)
}
