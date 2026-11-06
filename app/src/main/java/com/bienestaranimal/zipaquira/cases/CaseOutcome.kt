package com.bienestaranimal.zipaquira.cases

sealed class CaseOutcome {
    data class Saved(val caseId: String) : CaseOutcome()
    data class Updated(val caseId: String, val status: String) : CaseOutcome()
    data class CareSaved(val caseId: String) : CaseOutcome()
    data class Listed(val cases: List<CaseSummary>) : CaseOutcome()
    data class Error(val message: String) : CaseOutcome()
}
