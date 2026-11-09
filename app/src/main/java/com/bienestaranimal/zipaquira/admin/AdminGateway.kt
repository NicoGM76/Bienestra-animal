package com.bienestaranimal.zipaquira.admin

interface AdminGateway {
    fun loadDashboard(callback: (AdminOutcome) -> Unit)
    fun assignCase(caseId: String, veterinarianUid: String, callback: (AdminOutcome) -> Unit)
    fun reassignCase(caseId: String, veterinarianUid: String, callback: (AdminOutcome) -> Unit)
    fun updateCasePriority(caseId: String, priority: String, callback: (AdminOutcome) -> Unit)
}
