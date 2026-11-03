package com.bienestaranimal.zipaquira.cases

interface CaseGateway {
    fun saveCase(draft: CaseDraft, callback: (CaseOutcome) -> Unit)
    fun listMyCases(callback: (CaseOutcome) -> Unit)
}
