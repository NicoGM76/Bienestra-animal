package com.bienestaranimal.zipaquira.cases

interface CaseGateway {
    fun saveCase(draft: CaseDraft, callback: (CaseOutcome) -> Unit)
    fun listMyCases(callback: (CaseOutcome) -> Unit)
    fun listAssignedCases(callback: (CaseOutcome) -> Unit)
    fun updateAssignedCaseStatus(caseId: String, newStatus: String, callback: (CaseOutcome) -> Unit)
    fun listClinicalHistory(caseId: String, callback: (CaseOutcome) -> Unit)

    fun saveVeterinaryCare(
        caseId: String,
        diagnosis: String,
        observations: String,
        treatment: String,
        recommendations: String,
        callback: (CaseOutcome) -> Unit,
    )
}
