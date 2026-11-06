package com.bienestaranimal.zipaquira.admin

interface AdminGateway {
    fun loadDashboard(callback: (AdminOutcome) -> Unit)
    fun assignCase(caseId: String, veterinarianUid: String, callback: (AdminOutcome) -> Unit)
}
